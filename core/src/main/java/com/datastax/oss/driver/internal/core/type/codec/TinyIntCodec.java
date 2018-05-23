/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.core.type.codec;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.PrimitiveByteCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import java.nio.ByteBuffer;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class TinyIntCodec implements PrimitiveByteCodec {
  @Override
  public GenericType<Byte> getJavaType() {
    return GenericType.BYTE;
  }

  @Override
  public DataType getCqlType() {
    return DataTypes.TINYINT;
  }

  @Override
  public boolean accepts(Object value) {
    return value instanceof Byte;
  }

  @Override
  public boolean accepts(Class<?> javaClass) {
    return javaClass == Byte.class;
  }

  @Override
  public ByteBuffer encodePrimitive(byte value, ProtocolVersion protocolVersion) {
    ByteBuffer bytes = ByteBuffer.allocate(1);
    bytes.put(0, value);
    return bytes;
  }

  @Override
  public byte decodePrimitive(ByteBuffer bytes, ProtocolVersion protocolVersion) {
    if (bytes == null || bytes.remaining() == 0) {
      return 0;
    } else if (bytes.remaining() != 1) {
      throw new IllegalArgumentException(
          "Invalid 8-bits integer value, expecting 1 byte but got " + bytes.remaining());
    } else {
      return bytes.get(bytes.position());
    }
  }

  @Override
  public String format(Byte value) {
    return (value == null) ? "NULL" : Byte.toString(value);
  }

  @Override
  public Byte parse(String value) {
    try {
      return (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL"))
          ? null
          : Byte.parseByte(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          String.format("Cannot parse 8-bits int value from \"%s\"", value));
    }
  }
}