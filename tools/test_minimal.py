#!/usr/bin/env python3
"""Smoke-test: build a *minimal* valid TFLite file with no operators and
verify the FlatBuffer reader accepts it. Helps isolate FlatBuffer-building
bugs from TFLite-schema bugs.
"""

import flatbuffers


def make_buffer_with_data(builder, data: bytes) -> int:
    data_off = builder.StartVector(1, len(data), 1)
    builder.head = builder.head - len(data)
    builder.Bytes[builder.head:builder.head + len(data)] = data
    data_off = builder.EndVector(len(data))
    builder.StartObject(2)
    builder.PrependUOffsetTRelativeSlot(0, data_off, 0)
    return builder.EndObject()


def make_empty_buffer(builder) -> int:
    builder.StartObject(2)
    return builder.EndObject()


def main():
    builder = flatbuffers.Builder(1024)

    # --- Buffer table (one with data, one empty) ---
    weights_data = bytes(4)  # 4 bytes of zeros
    w_buf = make_buffer_with_data(builder, weights_data)
    e_buf = make_empty_buffer(builder)

    builder.StartVector(4, 2, 4)
    builder.PrependUOffsetTRelative(w_buf)
    builder.PrependUOffsetTRelative(e_buf)
    buffers_vec = builder.EndVector()

    # --- Operator code (NONE = 0) ---
    builder.StartObject(4)
    builder.PrependInt32Slot(0, 0, 0)  # builtin_code = 0 (ADD? or NONE?)
    op_code = builder.EndObject()
    builder.StartVector(4, 1, 4)
    builder.PrependUOffsetTRelative(op_code)
    op_codes_vec = builder.EndVector()

    # --- SubGraph (11 fields) ---
    name_off = builder.CreateString("min_graph")
    in_vec = builder.StartVector(4, 1, 4)
    builder.PrependInt32(0)
    in_vec = builder.EndVector()
    out_vec = builder.StartVector(4, 1, 4)
    builder.PrependInt32(0)
    out_vec = builder.EndVector()

    # Tensor 0: input/output [1, 1] float32, buffer 0 (empty)
    shape_vec = builder.StartVector(4, 2, 4)
    builder.PrependInt32(1)
    builder.PrependInt32(1)
    shape_vec = builder.EndVector()

    name2_off = builder.CreateString("x")
    builder.StartObject(7)
    empty_q = builder.EndObject()

    builder.StartObject(9)
    builder.PrependUOffsetTRelativeSlot(0, shape_vec, 0)
    builder.PrependInt8Slot(1, 0, 0)
    builder.PrependInt32Slot(2, 0, 0)
    builder.PrependUOffsetTRelativeSlot(3, name2_off, 0)
    builder.PrependUOffsetTRelativeSlot(4, empty_q, 0)
    tensor = builder.EndObject()

    builder.StartVector(4, 1, 4)
    builder.PrependUOffsetTRelative(tensor)
    tensors_vec = builder.EndVector()

    # No operators

    builder.StartObject(11)
    builder.PrependUOffsetTRelativeSlot(0, name_off, 0)
    builder.PrependUOffsetTRelativeSlot(1, tensors_vec, 0)
    builder.PrependUOffsetTRelativeSlot(2, in_vec, 0)
    builder.PrependUOffsetTRelativeSlot(3, out_vec, 0)
    sg_off = builder.EndObject()
    builder.StartVector(4, 1, 4)
    builder.PrependUOffsetTRelative(sg_off)
    subgraphs_vec = builder.EndVector()

    desc_off = builder.CreateString("min")

    builder.StartObject(8)
    builder.PrependInt32Slot(0, 3, 0)
    builder.PrependUOffsetTRelativeSlot(1, op_codes_vec, 0)
    builder.PrependUOffsetTRelativeSlot(2, subgraphs_vec, 0)
    builder.PrependUOffsetTRelativeSlot(3, desc_off, 0)
    builder.PrependUOffsetTRelativeSlot(4, buffers_vec, 0)
    model_off = builder.EndObject()
    builder.Finish(model_off)

    data = bytes(builder.Output())
    open("min.tflite", "wb").write(data)
    print(f"wrote min.tflite ({len(data)} bytes)")

    # Try parsing it
    from tflite import Model
    m = Model.GetRootAsModel(data, 0)
    print(f"version={m.Version()}, subgraphs={m.SubgraphsLength()}, "
          f"buffers={m.BuffersLength()}, opcodes={m.OperatorCodesLength()}")
    sg = m.Subgraphs(0)
    print(f"sg name={sg.Name()}, tensors={sg.TensorsLength()}")
    t = sg.Tensors(0)
    print(f"  tensor name={t.Name()}, shape={list(t.ShapeAsNumpy())}, type={t.Type()}")

    # Try ai-edge-litert
    try:
        from ai_edge_litert import interpreter as tflite
        itp = tflite.Interpreter(model_path="min.tflite")
        print("ai-edge-litert load: OK")
    except Exception as e:
        print(f"ai-edge-litert load: FAIL ({e})")


if __name__ == "__main__":
    main()
