package com.ThirtyNineEighty.Renderable.Resources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLUtils;

import com.ThirtyNineEighty.System.GameContext;

import java.io.InputStream;

public class FileTextureSource
  implements ISource<Texture>
{
  private final String name;
  private final boolean generateMipmap;

  public FileTextureSource(String textureName, boolean mipmap)
  {
    name = textureName;
    generateMipmap = mipmap;
  }

  @Override
  public String getName() { return name; }

  @Override
  public Texture load()
  {
    return new Texture(loadHandle());
  }

  @Override
  public void reload(Texture texture)
  {
    release(texture);
    texture.setHandle(loadHandle());
  }

  @Override
  public void release(Texture texture)
  {
    int handle = texture.getHandle();
    if (GLES20.glIsTexture(handle))
      GLES20.glDeleteTextures(1, new int[] { handle }, 0);
  }

  private int loadHandle()
  {
    try
    {
      String fileName = getTextureFileName(name);
      InputStream stream = GameContext.getAppContext().getAssets().open(fileName);
      Bitmap bitmap = BitmapFactory.decodeStream(stream);
      stream.close();

      int type = GLUtils.getType(bitmap);
      int format = GLUtils.getInternalFormat(bitmap);
      int error;

      int[] textures = new int[1];

      GLES20.glGenTextures(1, textures, 0);
      if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
        throw new GLException(error);

      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
      if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
        throw new GLException(error);

      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
      if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
        throw new GLException(error);

      GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, format, bitmap, type, 0);
      if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
        throw new GLException(error);

      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

      if (generateMipmap)
      {
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
          throw new GLException(error, Integer.toString(error));
      }

      bitmap.recycle();
      return textures[0];
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private static String getTextureFileName(String name)
  {
    return String.format("Textures/%s.png", name);
  }
}