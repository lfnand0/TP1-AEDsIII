package manager;

import java.io.*;

import dao.*;

public class Manager {
  private String dbPath = "";

  public Manager(String dbPath) {
    this.dbPath = dbPath;
  }

  private static boolean dbExists(String dbPath) {
    boolean exists = false;
    RandomAccessFile arq;

    try {
      arq = new RandomAccessFile(dbPath, "rw");
      arq.close();
    } catch (Exception e) {
      System.out.println("randomaccessfile error: " + e);
    }

    File f = new File(dbPath);
    if (f.exists() && !f.isDirectory()) {
      exists = true;
    }
    return exists;
  }

  /*
   * Get Max id
   * - Movimenta o ponteiro pro inicio do arquivo
   * - Le e retrona o maior id
   * - Ou soma +1 e o atualiza e o retorna
   */
  public int getMaxId(boolean update) {
    int maxId = 0;
    RandomAccessFile arq;

    dbExists(dbPath);

    try {
      arq = new RandomAccessFile(dbPath, "rw");

      arq.seek(0);
      maxId = arq.length() == 0 ? 0 : arq.readInt();
      if (update) {
        arq.seek(0);
        maxId++;
        arq.writeInt(maxId);
      }

      arq.close();
    } catch (Exception e) {
    }
    return maxId;
  }

  public int getMaxId() {
    return getMaxId(false);
  }

  /*
   * 
   * 
   * 
   * 
   */
  public static long findIdPointer(int id) {
    long returns = -1;
    RandomAccessFile arq;
    String dbPath = "../db/bank.db";

    try {
      arq = new RandomAccessFile(dbPath, "rw");

      arq.seek(0);
      int maxId = arq.readInt();

      if (id <= maxId) {
        int foundId = 0;
        long pointer = arq.getFilePointer();
        while (pointer < arq.length() - 4) {
          boolean lapide = arq.readBoolean();
          int tam = arq.readInt();

          foundId = arq.readInt();
          pointer = arq.getFilePointer();

          if (foundId != id || !lapide) {
            pointer += tam - 4; // antes da lapide
            arq.seek(pointer);
          } else {
            pointer -= 8;
            returns = pointer;
            break;
          }
        }
      }

      arq.close();
    } catch (Exception e) {
    }

    return returns;
  }

  /*
   * Create
   * - Movimento o ponteiro pro final do arquivo
   * - Insere a lapide
   * - Insere o tamanho do arquivo
   * - Insere dados do arquivo
   */
  public boolean appendToFile(byte[] arr) {
    RandomAccessFile arq;

    try {
      arq = new RandomAccessFile(dbPath, "rw");
      arq.seek(arq.length());
      arq.writeBoolean(true);
      arq.writeInt(arr.length);
      arq.write(arr);
      arq.close();
    } catch (Exception e) {
    }

    return true;
  }

  public static Dao read(int id) {
    byte[] ba;
    RandomAccessFile arq;
    Dao conta = new Dao();
    String dbPath = "../db/bank.db";

    if (findIdPointer(id) == -1 || id <= 0) {
      return conta;
    }

    try {
      arq = new RandomAccessFile(dbPath, "rw");
      arq.seek(findIdPointer(id));
      int tam = arq.readInt();
      ba = new byte[tam];
      arq.read(ba);
      conta.fromByteArray(ba);

      arq.close();
    } catch (Exception e) {
      System.out.println(e);
    }

    return conta;
  }

  public boolean update(byte[] ba, int id) {
    RandomAccessFile arq;

    if (ba.length == 0 || findIdPointer(id) == -1) {
      return false;
    }

    try {
      arq = new RandomAccessFile(dbPath, "rw");
      arq.seek(findIdPointer(id));
      int tam = arq.readInt();

      if (ba.length <= tam) {
        arq.write(ba);
      } else {
        delete(id);
        appendToFile(ba);
      }
    } catch (Exception e) {
      System.out.println(e);
    }

    return true;
  }

  public boolean delete(int id) {
    RandomAccessFile arq;
    boolean tf = false;

    try {
      arq = new RandomAccessFile(dbPath, "rw");
      long pos = findIdPointer(id) - 1;

      if (pos > 0) {
        arq.seek(pos);
        arq.writeBoolean(false);
        tf = true;
      }

      arq.close();
    } catch (Exception e) {
    }
    return tf;
  }
}
