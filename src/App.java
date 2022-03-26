import java.util.*;

import dao.*;

public class App {
    static Scanner sc = new Scanner(System.in);
    static Dao conta;

    public static int bankInterface() {
        int choice = 0;

        System.out.println("\n-----Conta Bancária-----");

        System.out.println("1. Criar uma conta bancária;");
        System.out.println("2. Realizar uma transferência;");
        System.out.println("3. Ler um registro;");
        System.out.println("4. Atualizar um registro;");
        System.out.println("5. Deletar um registro;");
        System.out.println("6. Sair;");

        System.out.print("\nEscolha uma opção: ");
        choice = sc.nextInt();
        while (choice < 1 || choice > 6) {
            System.out.print("Digite um valor de 1 a 6: ");
            choice = sc.nextInt();
        }

        int id;
        switch (choice) {
            case 1:
                System.out.print("\nDigite o nome: ");
                sc.nextLine();
                String nome = sc.nextLine();

                System.out.print("Digite o cpf: ");
                String cpf = sc.nextLine();

                System.out.print("Digite a cidade: ");
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
                System.out.print("\nDigite o ID da sua conta: ");
                int idA = sc.nextInt();
                conta = new Dao(idA);
                System.out.println(conta);

                System.out.print("\nDigite o ID da a conta que sera transferido: ");
                int idB = sc.nextInt();

                System.out.print("Digite o valor a ser transferido: ");
                float valor = sc.nextFloat();

                if (conta.transfer(idB, valor)) {
                    System.out.println("\nTransferencia bem sucedida seu saldo é de " + conta.getSaldoConta());
                } else {
                    System.out.println("\nID não encontrado ou saldo insuficiente");
                }
                break;

            case 3:
                System.out.print("\nDigite o ID da conta que deseja buscar: ");
                id = sc.nextInt();

                while (id <= 0) {
                    System.out.print("Digite um ID válido (maior que 0): ");
                    id = sc.nextInt();
                }

                conta = new Dao(id);

                if (conta.getId() != -1) {
                    System.out.println(conta);
                } else {
                    System.out.println("Conta não encontrada.");
                }

                break;

            case 4:
                System.out.print("\nDigite o ID da conta a ser atualizada: ");
                id = sc.nextInt();

                while (id <= 0) {
                    System.out.print("Digite um ID válido (maior que 0): ");
                    id = sc.nextInt();
                }

                conta = new Dao(id);
                System.out.println(conta);

                boolean worked = conta.update();
                if (worked) {
                    System.out.println("Conta atualizada com sucesso!");
                } else {
                    System.out.println("Erro ao atualizar conta.");
                }

                break;

            case 5:
                System.out.print("\nDigite o ID da conta a ser deletada: ");
                id = sc.nextInt();

                while (id <= 0) {
                    System.out.print("Digite um ID válido (maior que 0): ");
                    id = sc.nextInt();
                }

                conta = new Dao();

                if (conta.delete(id)) {
                    System.out.println("A conta de ID " + id + " foi deletada com sucesso");
                } else {
                    System.out.println("Erro ao deletar conta de ID " + id);
                }
                break;

            case 6:

                break;
        }

        return choice;
    }

    public static void main(String[] args) throws Exception {
        while (bankInterface() != 6)
            ;

        sc.close();
    }
}