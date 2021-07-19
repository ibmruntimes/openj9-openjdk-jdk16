/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * ===========================================================================
 * (c) Copyright IBM Corp. 2021, 2021 All Rights Reserved
 * ===========================================================================
 */

package jdk.internal.foreign.abi.ppc64.sysv;

import jdk.incubator.foreign.*;
import jdk.incubator.foreign.CLinker.VaList;
import jdk.internal.foreign.abi.SharedUtils;
import jdk.internal.foreign.abi.SharedUtils.SimpleVaArg;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static jdk.internal.foreign.PlatformLayouts.SysVppc64le.C_POINTER;

/**
 * This file is copied from x86/windows (Windows/x86_64) as a placeholder for compilation
 * as VaList on Linux/ppc64le at Java level is not yet implemented for the moment.
 * The defintion VaList must map to the underlying struct of va_list defined on Linux/ppc64le
 * which might be similar to Windows/x86_64. Thus, futher analysis on the struct is required
 * to understand how the struct is laid out in memory (e.g. the type & size of each field in
 * va_list) and how the registers are allocated for va_list.
 */
public class SysVVaList implements VaList {
    static final Class<?> CARRIER = MemoryAddress.class;
    private static final long VA_SLOT_SIZE_BYTES = 8;
    private static final VarHandle VH_address = MemoryHandles.asAddressVarHandle(C_POINTER.varHandle(long.class));
    private static final VaList EMPTY = new SharedUtils.EmptyVaList(MemoryAddress.NULL);

    private MemorySegment segment;
    private final List<MemorySegment> attachedSegments;
    private final MemorySegment livenessCheck;

    private SysVVaList(MemorySegment segment, List<MemorySegment> attachedSegments, MemorySegment livenessCheck) {
        this.segment = segment;
        this.attachedSegments = attachedSegments;
        this.livenessCheck = livenessCheck;
    }

    public static final VaList empty() {
        return EMPTY;
    }

    @Override
    public int vargAsInt(MemoryLayout layout) {
        //return (int) read(int.class, layout);
        throw new InternalError("vargAsInt is not yet implemented"); //$NON-NLS-1$
    }

    @Override
    public long vargAsLong(MemoryLayout layout) {
        //return (long) read(long.class, layout);
        throw new InternalError("vargAsLong is not yet implemented"); //$NON-NLS-1$
    }

    @Override
    public double vargAsDouble(MemoryLayout layout) {
        //return (double) read(double.class, layout);
        throw new InternalError("vargAsDouble is not yet implemented"); //$NON-NLS-1$
    }

    @Override
    public MemoryAddress vargAsAddress(MemoryLayout layout) {
        //return (MemoryAddress) read(MemoryAddress.class, layout);
        throw new InternalError("vargAsAddress is not yet implemented"); //$NON-NLS-1$
    }

    @Override
    public MemorySegment vargAsSegment(MemoryLayout layout) {
        //return (MemorySegment) read(MemorySegment.class, layout);
        throw new InternalError("vargAsSegment is not yet implemented"); //$NON-NLS-1$
    }

    @Override
    public MemorySegment vargAsSegment(MemoryLayout layout, NativeScope scope) {
        //Objects.requireNonNull(scope);
        //return (MemorySegment) read(MemorySegment.class, layout, SharedUtils.Allocator.ofScope(scope));
        throw new InternalError("vargAsSegment is not yet implemented"); //$NON-NLS-1$
    }

    private Object read(Class<?> carrier, MemoryLayout layout) {
        return read(carrier, layout, MemorySegment::allocateNative);
    }

    private Object read(Class<?> carrier, MemoryLayout layout, SharedUtils.Allocator allocator) {
        Objects.requireNonNull(layout);
        SharedUtils.checkCompatibleType(carrier, layout, SysVppc64leLinker.ADDRESS_SIZE);
        Object res;
        if (carrier == MemorySegment.class) {
            TypeClass typeClass = TypeClass.typeClassFor(layout);
            res = switch (typeClass) {
                case STRUCT_REFERENCE -> {
                    MemoryAddress structAddr = (MemoryAddress) VH_address.get(segment);
                    try (MemorySegment struct = handoffIfNeeded(structAddr.asSegmentRestricted(layout.byteSize()),
                         segment.ownerThread())) {
                        MemorySegment seg = allocator.allocate(layout.byteSize());
                        seg.copyFrom(struct);
                        yield seg;
                    }
                }
                case STRUCT_REGISTER -> {
                    MemorySegment struct = allocator.allocate(layout);
                    struct.copyFrom(segment.asSlice(0L, layout.byteSize()));
                    yield struct;
                }
                default -> throw new IllegalStateException("Unexpected TypeClass: " + typeClass);
            };
        } else {
            VarHandle reader = SharedUtils.vhPrimitiveOrAddress(carrier, layout);
            res = reader.get(segment);
        }
        segment = segment.asSlice(VA_SLOT_SIZE_BYTES);
        return res;
    }

    @Override
    public void skip(MemoryLayout... layouts) {
        //Objects.requireNonNull(layouts);
        //Stream.of(layouts).forEach(Objects::requireNonNull);
        //segment = segment.asSlice(layouts.length * VA_SLOT_SIZE_BYTES);
        throw new InternalError("skip is not yet implemented"); //$NON-NLS-1$
    }

    static SysVVaList ofAddress(MemoryAddress addr) {
        //MemorySegment segment = addr.asSegmentRestricted(Long.MAX_VALUE);
        //return new SysVVaList(segment, List.of(segment), null);
        throw new InternalError("ofAddress is not yet implemented"); //$NON-NLS-1$
    }

    static Builder builder(SharedUtils.Allocator allocator) {
        return new Builder(allocator);
    }

    @Override
    public void close() {
        if (livenessCheck != null)
            livenessCheck.close();
        attachedSegments.forEach(MemorySegment::close);
    }

    @Override
    public VaList copy() {
        MemorySegment liveness = handoffIfNeeded(MemoryAddress.NULL.asSegmentRestricted(1),
                segment.ownerThread());
        return new SysVVaList(segment, List.of(), liveness);
    }

    @Override
    public VaList copy(NativeScope scope) {
        Objects.requireNonNull(scope);
        MemorySegment liveness = handoffIfNeeded(MemoryAddress.NULL.asSegmentRestricted(1),
                segment.ownerThread());
        liveness = liveness.handoff(scope);
        return new SysVVaList(segment, List.of(), liveness);
    }

    @Override
    public MemoryAddress address() {
        return segment.address();
    }

    @Override
    public boolean isAlive() {
        if (livenessCheck != null)
            return livenessCheck.isAlive();
        return segment.isAlive();
    }

    static class Builder implements VaList.Builder {

        private final SharedUtils.Allocator allocator;
        private final List<SimpleVaArg> args = new ArrayList<>();

        public Builder(SharedUtils.Allocator allocator) {
            this.allocator = allocator;
        }

        private Builder arg(Class<?> carrier, MemoryLayout layout, Object value) {
            Objects.requireNonNull(layout);
            Objects.requireNonNull(value);
            SharedUtils.checkCompatibleType(carrier, layout, SysVppc64leLinker.ADDRESS_SIZE);
            args.add(new SimpleVaArg(carrier, layout, value));
            return this;
        }

        @Override
        public Builder vargFromInt(ValueLayout layout, int value) {
            //return arg(int.class, layout, value);
            throw new InternalError("vargFromInt is not yet implemented"); //$NON-NLS-1$
        }

        @Override
        public Builder vargFromLong(ValueLayout layout, long value) {
            //return arg(long.class, layout, value);
            throw new InternalError("vargFromLong is not yet implemented"); //$NON-NLS-1$
        }

        @Override
        public Builder vargFromDouble(ValueLayout layout, double value) {
            //return arg(double.class, layout, value);
            throw new InternalError("vargFromDouble is not yet implemented"); //$NON-NLS-1$
        }

        @Override
        public Builder vargFromAddress(ValueLayout layout, Addressable value) {
            //return arg(MemoryAddress.class, layout, value.address());
            throw new InternalError("vargFromAddress is not yet implemented"); //$NON-NLS-1$
        }

        @Override
        public Builder vargFromSegment(GroupLayout layout, MemorySegment value) {
            //return arg(MemorySegment.class, layout, value);
            throw new InternalError("vargFromSegment is not yet implemented"); //$NON-NLS-1$
        }

        public VaList build() {
            if (args.isEmpty()) {
                return EMPTY;
            }
            MemorySegment segment = allocator.allocate(VA_SLOT_SIZE_BYTES * args.size());
            List<MemorySegment> attachedSegments = new ArrayList<>();
            attachedSegments.add(segment);
            MemorySegment cursor = segment;

            for (SimpleVaArg arg : args) {
                if (arg.carrier == MemorySegment.class) {
                    MemorySegment msArg = ((MemorySegment) arg.value);
                    TypeClass typeClass = TypeClass.typeClassFor(arg.layout);
                    switch (typeClass) {
                        case STRUCT_REFERENCE -> {
                            MemorySegment copy = allocator.allocate(arg.layout);
                            copy.copyFrom(msArg); // by-value
                            attachedSegments.add(copy);
                            VH_address.set(cursor, copy.address());
                        }
                        case STRUCT_REGISTER -> {
                            MemorySegment slice = cursor.asSlice(0, VA_SLOT_SIZE_BYTES);
                            slice.copyFrom(msArg);
                        }
                        default -> throw new IllegalStateException("Unexpected TypeClass: " + typeClass);
                    }
                } else {
                    VarHandle writer = arg.varHandle();
                    writer.set(cursor, arg.value);
                }
                cursor = cursor.asSlice(VA_SLOT_SIZE_BYTES);
            }

            return new SysVVaList(segment, attachedSegments, null);
        }
    }

    private static MemorySegment handoffIfNeeded(MemorySegment segment, Thread thread) {
        return segment.ownerThread() == thread ?
                segment : segment.handoff(thread);
    }
}
