package io.github.alanpeng899.shiro.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author pengyq<br>
 *  创建时间：2019年5月30日<br>
 *  类说明：自定义object序列化，反序列化
 */
public class ObjectSerializer implements RedisSerializer<Object> {

	public static final int BYTE_ARRAY_OUTPUT_STREAM_SIZE = 128;

	@Override
	public byte[] serialize(Object object) {
		byte[] result = new byte[0];

		if (object == null) {
			return result;
		}
		if (!(object instanceof Serializable)) {
			throw new SerializationException("requires a Serializable payload " + "but received an object of type ["
					+ object.getClass().getName() + "]");
		}

		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BYTE_ARRAY_OUTPUT_STREAM_SIZE);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);) {
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
			result = byteStream.toByteArray();
		} catch (IOException e) {
			throw new SerializationException("serialize error, object=" + object, e);
		}

		return result;
	}

	@Override
	public Object deserialize(byte[] bytes) {
		Object result = null;

		if (bytes == null || bytes.length == 0) {
			return result;
		}

		try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
				ObjectInputStream objectInputStream = new MultiClassLoaderObjectInputStream(byteStream);) {
			result = objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new SerializationException("deserialize error", e);
		}

		return result;
	}

}
