import java.util.*;

import dao.*;

public class App {
    static Scanner sc = new Scanner(System.in);
    static Dao conta;

    public static int bankInterface() {
        int choice = 0;

        System.out.println("-----Conta Bancária-----");

        System.out.println("1. Criar uma conta bancária;");
        System.out.println("2. Realizar uma transferência;");
        System.out.println("3. Ler um registro;");
        System.out.println("4. Atualizar um registro;");
        System.out.println("5. Deletar um registro;");
        System.out.println("6. Sair;");

        System.out.print("Escolha uma opção: ");
        choice = sc.nextInt();
        while (choice < 1 || choice > 6) {
            System.out.print("Digite um valor de 1 a 6: ");
            choice = sc.nextInt();
        }

        switch (choice) {
            case 1:
                System.out.print("\nDigite o nome: ");
                sc.nextLine();
                String nome = sc.nextLine();

                System.out.print("Digite o cpf: ");
                // sc.nextLine();
                String cpf = sc.nextLine();

                System.out.print("Digite a cidade: ");
                // sc.nextLine();
                String cidade = sc.nextLine();

                conta = new Dao(nome, cpf, cidade);
                int resust = conta.create();

                if (resust != -1) {
                    System.out.print("\n CONTA CRIADA COM SUCESSO");
                    System.out.println(conta.toString());
                } else {
                    System.out.println("Houve um ERRO ao tentar criar sua conta");
                }
                break;

            case 2:

                break;

            case 3:
                System.out.print("\nDigite o ID da conta que deseja buscar: ");
                int idRead = sc.nextInt();

                while (idRead <= 0) {
                    System.out.print("Digite um ID válido (maior que 0): ");
                    idRead = sc.nextInt();
                }

                conta = new Dao();
                conta = conta.read(idRead);

                if (conta.getId() != -1) {
                    System.out.println(conta);
                } else {
                    System.out.println("Conta não encontrada.");
                }

                break;

            case 4:
                System.out.print("\nDigite o ID da conta a ser atualizada: ");
                int idUpdate = sc.nextInt();

                while (idUpdate <= 0) {
                    System.out.print("Digite um ID válido (maior que 0): ");
                    idUpdate = sc.nextInt();
                }

                conta = new Dao();
                conta = conta.read(idUpdate);
                // System.out.println("Debug main.update: idUpdate = " + idUpdate);
                conta.toString();
                boolean worked = conta.update();
                if (worked) {
                    System.out.println("Conta atualizada com sucesso!");
                } else {
                    System.out.println("Erro ao atualizar conta.");
                }

                break;

            case 5:
                System.out.print("\nDigite o ID da conta a ser deletada: ");
                int idDelete = sc.nextInt();

                while (idDelete <= 0) {
                    System.out.print("Digite um ID válido (maior que 0): ");
                    idDelete = sc.nextInt();
                }

                conta = new Dao();

                if (conta.delete(idDelete)) {
                    System.out.println("A conta de ID " + idDelete + " foi deletada com sucesso");
                } else {
                    System.out.println("Erro ao deletar conta de ID " + idDelete);
                }
                break;

            case 6:

                break;
        }

        sc.close();
        return choice;
    }

    public static void main(String[] args) throws Exception {
        bankInterface();
    }
}