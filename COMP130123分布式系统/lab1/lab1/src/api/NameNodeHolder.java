package api;

/**
* api/NameNodeHolder.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月13日 星期一 下午07时29分00秒 CST
*/

public final class NameNodeHolder implements org.omg.CORBA.portable.Streamable
{
  public api.NameNode value = null;

  public NameNodeHolder ()
  {
  }

  public NameNodeHolder (api.NameNode initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = api.NameNodeHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    api.NameNodeHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return api.NameNodeHelper.type ();
  }

}
