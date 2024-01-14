package api;


/**
* api/NameNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月13日 星期一 下午07时29分00秒 CST
*/

public interface NameNodeOperations 
{

  //TODO: complete the interface design
  String open (String filePath, int mode);
  void close (String filePath);
} // interface NameNodeOperations
