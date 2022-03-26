package manager;

import java.io.*;

import dao.*;

/**
 * CLASSE MANAGER
 * 
 * Nível mais baixo de execução, realiza operações
 * diretamente no arquivo, como pesquisa,
 * alteração, adição e remoção de registros, etc.
 * 
 * Recebe ordens da DAO
 * 
 */
public class Manager {
  /**
   * dbPath: diretório onde a base de dados será armazenada
   */
  private String dbPath = "";

  /**
   * Construtor básico
   * 
   * @param dbPath: diretório da base de dados, que é salvo
   *                como um atributo no objeto
   */
  public Manager(String dbPath) {
    this.dbPath = dbPath;
  }

  /**
   * Função auxiliar que checa se o arquivo da base de dados
   * está presente no dbPath, e caso não esteja, o cria.
   * 
   * @param dbPath: Diretório da base de dados
   * @return boolean: true ou false caso a operação seja
   *         bem-sucedida ou não
   */
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

  /**
   * Função que encontra o maior ID presente no cabeçalho do
   * arquivo
   * 
   * @param update: caso seja true, reescreve o valor do cabeçalho,
   *                adicionando +1 a ele
   * @return int: retorna o valor do maior ID do arquivo
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

  /**
   * Busca a posição do ponteiro no início do registro cujo ID
   * é equivalente ao recebido por parâmetro
   * 
   * O ponteiro retornado é na posição da lápide do registro,
   * portanto caso realizemos a leitura a partir de então, teremos
   * inicialmente o tamanho do array de bytes, seguido pelo array
   * em si
   * 
   * @param id: ID do registro a ser pesquisado
   * @return long: ponteiro do registro
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
        /**
         * Ao atualizarmos um registro, existe a possibilidade
         * de que um registro com o mesmo ID permaneça na posição
         * antiga, porém com a lápide = false
         * 
         * Por isso, é importante, durante a pesquisa, checar a
         * lápide do registro, o tamanho do array de bytes e o ID
         * 
         * Antes do if...else seguinte, o ponteiro estará posicionado
         * após o ID --- caso não seja o registro que estamos buscando,
         * voltamos 4 casas para nos posicionarmos antes do array de
         * bytes, e depois pulamos para depois do array, na posição
         * exata do início do próximo registro.
         * 
         * Esse processo é realizado até chegarmos no fim do arquivo,
         * ou encontrarmos o registro pesquisado
         */
        while (pointer < arq.length() - 4) {
          boolean lapide = arq.readBoolean();
          int tam = arq.readInt();

          foundId = arq.readInt();
          pointer = arq.getFilePointer();

          if (foundId != id || !lapide) {
            pointer += tam - 4; // Antes da lápide
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

  /**
   * Adiciona um array de bytes ao fim do arquivo
   * 
   * @param ba: Array de bytes a ser adicionado ao arquivo
   * @return boolean: true ou false caso a operação seja
   *         bem-sucedida
   */
  public boolean appendToFile(byte[] ba) {
    RandomAccessFile arq;

    try {
      arq = new RandomAccessFile(dbPath, "rw");
      arq.seek(arq.length());
      arq.writeBoolean(true);
      arq.writeInt(ba.length);
      arq.write(ba);
      arq.close();
    } catch (Exception e) {
    }

    return true;
  }

  /**
   * Função para gerar um objeto DAO a partir
   * do registro cujo ID seja equivalente ao
   * recebido como parâmetro
   * 
   * @param id: ID do registro a ser lido
   * @return Dao: retorna um objeto inválido caso o ID
   *         seja inválido, ou um objeto contendo os atributos
   *         coletados do registro
   */
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

  /**
   * Função de atualização de registros
   * 
   * Caso o novo array de bytes possua um tamanho maior que o
   * do seu registro original, alteramos a lápide do original
   * para false e adicionamos o registro atualizado ao fim do
   * arquivo; Caso possua um tamanho menor ou igual ao registro
   * original, sobrescrevemos o registro com o novo array de
   * bytes
   * 
   * @param ba: Novo array de bytes
   * @param id: ID do registro a ser atualizado
   * @return boolean: true ou false caso a operação seja
   *         bem-sucedida
   */
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

  /**
   * Altera a lápide de um registro para false
   * 
   * @param id: ID do registro a ser deletado
   * @return boolean: true ou false caso a operação seja
   *         bem-sucedida
   */
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
