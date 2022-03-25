package manager;

import java.io.*;

import dao.*;

public class Manager {
  private String dbPath = "";

  public Manager(String dbPath) {
    this.dbPath = dbPath;
  }

  private boolean dbExists() {
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

    dbExists();
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
  public long findIdPointer(int id) {
    long returns = -1;
    RandomAccessFile arq;

    try {
      arq = new RandomAccessFile(dbPath, "rw");

      arq.seek(0);
      int maxId = arq.readInt();
      // System.out.println("findIdPointer debug: maxId = " + maxId + "; id = " + id);
      
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

      // System.out.println("returns = " + returns);

      arq.close();
    } catch (Exception e) {
    }

    // System.out.println("returns = " + returns);
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

  public Dao read(int id){
    byte[] ba;
    RandomAccessFile arq;
    Dao conta = new Dao();

    if (findIdPointer(id) == -1) {
      return conta;
    }

    try{
      arq = new RandomAccessFile(dbPath, "rw");
      
      arq.seek(findIdPointer(id) + 4); // +4 pois o findIdPointer retorna a posicao antes do registro tam
      // int tam = arq.readInt();
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      
      int idConta = arq.readInt();
      String nomePessoa = arq.readUTF();
      String cpf = arq.readUTF();
      String cidade = arq.readUTF();
      int transferenciasRealizadas = arq.readInt();
      float saldoConta = arq.readFloat();
      System.out.println("read debug");
      
      dos.writeInt(idConta);
      dos.writeUTF(nomePessoa);
      dos.writeUTF(cpf);
      dos.writeUTF(cidade);
      dos.writeInt(transferenciasRealizadas);
      dos.writeFloat(saldoConta);

      ba = baos.toByteArray();
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
      System.out.println("Debug manager.update: id = " + id + "; " + findIdPointer(id));
      arq.seek(findIdPointer(id));
      int tam = arq.readInt();

      if (ba.length <= tam) {
        System.out.println("man.update first if");
        arq.write(ba);
      } else {
        Dao temp = new Dao();
        temp = temp.read(id);
        System.out.println("man.update second if");
        delete(id);
        appendToFile(ba);
      }
      System.out.println("man.update after if");
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
