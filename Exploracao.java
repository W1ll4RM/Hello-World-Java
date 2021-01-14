import java.io.*;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

// isso é um jogo meio bosta que fiz apenas para treinar. É inspirado no jogo Hades, que é um dos meus jogos favoritos
// o objetivo do jogo é bem simples: avançar o máximo possível para bater os próprios recordes de câmaras alcançadas
public class Exploracao {
    private int din, recorde, atual;            // quantidade de dinheiro do jogador, recorde de câmaras visitadas no jogo
    private float atk, def, lif;                // atributos do jogador
    private float enAtk, enDef, enLif;          // atributos dos inimigos (computador)
    private boolean morreu;                     // verifica se o jogador morreu, para zerar seus atributos caso sim

    public Exploracao() {
        // cria um diretório para salvar o jogo. O fiz na pasta pública, que acredito computador possui, para evitar possíveis erros
        // ..com o nome do usuário, que poderia ser tido por meio do comando System.getProperty("user.name")
        new File("C:\\Users\\Public\\Documents\\DungeonSave").mkdirs();

        // atributos iniciais do jogador. São alterados caso o jogador tenha um save válido
        this.atk = 3;
        this.def = 1.7f;
        this.lif = 15;
        this.din = 0;
        this.recorde = 0;
        this.atual = 0;
        this.morreu = false;

        // atributos iniciais dos inimigos
        this.enAtk = 4;
        this.enDef = 1.2f;
        this.enLif = 10;

        // verifica o save e carrega os dados de usuário se tudo ocorrer bem durante a verificação
        if (this.verificarSave()) {
            this.carregar();
        }
    }

    // método main
    public static void main(String[] args) {
        Exploracao exp =  new Exploracao();
        exp.escolherCaminho();
    }

    // método que cria o arquivo txt que irá salvar o progresso feito no jogo se já não existir ou estiver corrompido
    // também, reaproveitei este método para resetar os atributos do jogador caso ele morra
    // retorna: false, se o arquivo não existia ou estava corrompido e teve que ser criado do zero
    // retorna: true, se o arquivo já existe e é um save válido
    public boolean verificarSave() {
        File file = new File("C:\\Users\\Public\\Documents\\DungeonSave\\Save.txt");

        // se o arquivo existir e não for um caso de morte do jogador, vai verificar se é um save válido e prosseguir
        if (file.exists() && !this.morreu) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                try {
                    if (reader.readLine().equals("true")) {
                        Float.parseFloat(reader.readLine());
                        Float.parseFloat(reader.readLine());
                        Float.parseFloat(reader.readLine());
                        Integer.parseInt(reader.readLine());
                        Integer.parseInt(reader.readLine());

                        return true;
                    }
                } catch (NumberFormatException e) {
                    file.delete();  // se tiver dados inválidos, o arquivo será apagado para ser escrito do zero
                    System.out.println("Save corrompido. Escrevendo novo save...");
                } catch (NullPointerException e) {
                    // se o arquivo estiver vazio, irá prosseguir para a escritura dos dados padrão
                    System.out.println("Save vazio. Escrevendo novo save...");
                }
            } catch (IOException e) {
                System.out.println("Arquivo provavelmente foi apagado durante a verificação");
            }
        }

        // verifica se o arquivo existe e é válido
        else if (!file.exists()) {
            System.out.println("Criando arquivo...");
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Impossível criar o arquivo");
            }
        }

        // tenta criar o escritor de arquivo e escrever os parâmetros informados, que são passados pelo construtor
        try {
            //BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            FileWriter writer = new FileWriter(file);
            // dizem que o BufferedWriter é mais eficiente. Irei usar só por hábito, mesmo sendo uma tarefa simples
            // só é possível, até onde sei, escrever em formato de String. No método carregar() tudo é convertido de volta
            writer.write("true\r\n" + this.atk + "\r\n" + this.def + "\r\n" + this.lif + "\r\n" + this.din + "\r\n" + this.recorde);
            writer.close();
        } catch (IOException IOEx) {
            System.out.println("Impossível criar o escritor de arquivos");
        }
        return false;
    }

    // método feito para salvar o jogo durante a jogatina. Seu propósito é totalmente diferente do método verificarSave, que serve para verificar e criar saves
    public void salvar() {
        File file = new File("C:\\Users\\Public\\Documents\\DungeonSave\\Save.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("true\r\n" + this.atk + "\r\n" + this.def + "\r\n" + this.lif + "\r\n" + this.din + "\r\n" + this.recorde);
            writer.close();
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o jogo");
            this.verificarSave();
        }
    }

    // método que lê o arquivo txt e carrega o progresso feito no jogo
    public void carregar() {
        File file = new File("C:\\Users\\Public\\Documents\\DungeonSave\\Save.txt");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            // tudo é escrito em formato de String e depois é convertido (se possível) para float e então inserido nas variáveis
            reader.readLine();
            this.atk = Float.parseFloat(reader.readLine());
            this.def = Float.parseFloat(reader.readLine());
            this.lif = Float.parseFloat(reader.readLine());
            this.din = Integer.parseInt(reader.readLine());
            this.recorde = Integer.parseInt(reader.readLine());
            reader.close();
        } catch (IOException IOEx) {
            System.out.println("Impossível criar o leitor de arquivo");
        } catch (NumberFormatException NumFEx) {
            System.out.println("Erro durante a leitura de save: dados inválidos. Criando novo save...");
            this.verificarSave();
        } catch (NullPointerException NullEx) {
            System.out.println("Erro durante a leitura do save: o arquivo está vazio. Criando novo save...");
            this.verificarSave();
        }

    }

    // método que permite ao jogador escolher um caminho para prosseguir no jogo. O mais importante do jogo
    // este método é uma bagunça difícil de entender. Serve como um meio de conexão para quase todos os outros métodos
    // apenas chamando este método já é possível fazer com que tudo funcione normalmente
    public void escolherCaminho() throws NoSuchElementException {
        Random rdm = new Random();
        Scanner in = new Scanner(System.in);

        System.out.println("Digite o caminho pelo qual deseja seguir: DIREITA OU ESQUERDA (status para ver seu estado atual)");
        String caminho = in.next();
        //in.close(); tive que remover o método que fecha o Scanner porque ocorria um bug quando o Scanner de um método fechava, comprometendo os Scanners dos outros
        if (caminho.equalsIgnoreCase("esquerda")) {
            // alguns valores são repetidos com um certo propósito. Há duas chances de ocorrer uma luta para o jogo não ficar tão chato
            // caso esquerda, há maior chance de vir um aprimoramento. Caso esquerda, há maior chance de vir uma loja
            switch (rdm.nextInt(7)) {
                case 0:
                    this.avancar();
                    this.aprimorar();
                    this.escolherCaminho();
                    break;
                case 1:
                    this.avancar();
                    if (this.lutar()) {
                        this.escolherCaminho();
                    } else {
                        System.out.println("Infelizmente, você morreu.");
                        this.morrer();
                        this.verificarSave();
                    }
                    break;
                case 2:
                    this.avancar();
                    this.aprimorarInimigo();
                    if (this.lutar()) {
                        this.escolherCaminho();
                    } else {
                        System.out.println("Você morreu!");
                        this.morrer();
                        this.verificarSave();
                    }
                    break;
                case 3:
                    this.avancar();
                    this.aprimorar();
                    this.aprimorarInimigo();
                    this.escolherCaminho();
                    break;
                case 4:
                    System.out.println("Nada aconteceu. +10 moedas!");
                    this.din += 10;
                    this.escolherCaminho();
                    break;
                case 5:
                    this.avancar();
                    this.loja();
                    this.escolherCaminho();
                    break;
                case 6:
                    this.avancar();
                    // aqui os inimigos ficam mais fortes
                    System.out.println("Que azar! Você foi amaldiçoado: seus inimigos agora são mais fortes");
                    this.aprimorarInimigo();
                    this.escolherCaminho();
                    break;
            }
        } else if (caminho.equalsIgnoreCase("direita")) {
            switch (rdm.nextInt(5)) {
                case 0:
                    this.avancar();
                    this.aprimorar();
                    this.escolherCaminho();
                    break;
                case 1:
                    this.avancar();
                    this.aprimorarInimigo();    // em uma das duas chances de vir luta, os inimigos ficam mais fortes antes da luta
                    if (this.lutar()) {
                        this.escolherCaminho();
                    } else {
                        System.out.println("Morto! Você alcancou a câmara: " + this.atual);
                        this.morrer();
                        this.verificarSave();
                    }
                    break;
                case 2:
                    this.avancar();
                    if (this.lutar()) {
                        this.escolherCaminho();
                    } else {
                        System.out.println("Diga ao Michael Jackson que eu mandei um abraço");
                        this.morrer();
                        this.verificarSave();
                    }
                    break;
                case 3:
                    this.avancar();
                    this.loja();
                    this.escolherCaminho();
                    break;
                case 4:
                    System.out.println("Nada aconteceu. +10 moedas!");
                    this.din += 10;
                    this.escolherCaminho();
                    break;
                case 5:
                    this.avancar();
                    this.loja();            // e o mesmo com as lojas
                    this.aprimorarInimigo();
                    this.escolherCaminho();
                    break;
                case 6:
                    this.avancar();
                    // aqui os inimigos ficam mais fortes
                    System.out.println("Você abriu uma caixa amaldiçoada, e agora seus inimigos agora são mais fortes");
                    this.aprimorarInimigo();
                    this.escolherCaminho();
                    break;
            }
        } else if (caminho.equalsIgnoreCase("status")) {
            System.out.println(this.status());
            this.escolherCaminho();
        } else if (caminho.equalsIgnoreCase("sair")) {
            this.encerrar();
        } else {
            System.out.println("Caminho inválido! Digite ESQUERDA ou DIREITA");
            this.escolherCaminho();
        }
    }

    // método que eventualmente aparece enquanto joga, permitindo aprimorar um dos 3 atributos do jogador
    public void aprimorar() {
        Scanner in = new Scanner(System.in);
        Random rdm = new Random();

        System.out.println("Qual dos seus atributos você deseja aprimorar: \r\nAtaque (atual: " + this.atk +
                "), Defesa: (atual: " + this.def + ") e Vida (" + this.lif + ")");
        String escolha = in.next();
        //in.close();
        if (escolha.equalsIgnoreCase("ataque")) {
            // aqui são sorteados valores de acordo com a escolha do jogador, para um aprimoramento
            this.atk *= rdm.nextFloat() + rdm.nextInt(3);
            System.out.println("Você aumentou sua quantidade de ataque para: " + this.atk);
        } else if (escolha.equalsIgnoreCase("defesa")) {
            this.def *= rdm.nextFloat() + rdm.nextInt(3);
            System.out.println("Você aumentou sua quantidade de defesa para: " + this.def);
        } else if (escolha.equalsIgnoreCase("vida")) {
            this.lif *= rdm.nextFloat() + rdm.nextInt(3);
            System.out.println("Você aumentou sua quantidade de vida para: " + this.lif);
        } else {
            System.out.println("Comando incorreto.");
            this.aprimorar();
        }

    }

    // aprimora um atributo aleatório do inimigo, para aumentar a dificuldade do jogo
    public void aprimorarInimigo() {
        Random rdm = new Random();
        switch (rdm.nextInt(3)) {
            case 0:
                this.enLif *= rdm.nextFloat() + rdm.nextInt(7);
                break;
            case 1:
                this.enAtk *= rdm.nextFloat() + rdm.nextInt(6);
                break;
            case 2:
                this.enAtk *= rdm.nextFloat() + rdm.nextInt(5);
                break;
        }
    }

    // para os inimigos não agirem 100% aleatoriamente, criei algo mais elaborado. Não é uma IA, mas deve servir para um projeto tão simples
    // retorna: VERDADEIRO caso venca a luta, FALSO caso perca a luta
    public boolean lutar() {
        Random rdm = new Random();
        Scanner in = new Scanner(System.in);

        System.out.println("Um inimigo! Derrote-o para avançar");

        do {
            System.out.println("Deseja ATACAR ou REVIDAR?");
            String resposta = in.next();

            int acaoInimiga = 0;
            // inimigo decide o que fazer
            if (enLif >= 10 || enLif < 5) {
                acaoInimiga = rdm.nextInt(3);
            } else if (enLif >= 5) {
                acaoInimiga = rdm.nextInt(2);
            }

            if (resposta.equalsIgnoreCase("atacar")) {
                if (acaoInimiga == 2 || acaoInimiga == 1) { // inimigo decide atacar
                    if (rdm.nextBoolean()) {
                        System.out.println("Seu inimigo tentou reagir com um ataque mas você foi mais rápido");
                        this.enLif -= this.atk * this.def;
                        System.out.println("Quantidade atual de vida: " + this.lif);
                    } else {
                        System.out.println("Seu inimigo atacou mais rápido do que você");
                        this.lif -= this.enAtk;
                        System.out.println("Quantidade atual de vida: " + this.lif);
                    }
                } else {
                    if (rdm.nextBoolean()) {            // inimigo decide revidar
                        System.out.println("Seu inimigo tentou reagir ao seu ataque, mas foi lento demais.");
                        this.enLif -= this.atk;
                        System.out.println("Quantidade atual de vida: " + this.lif);
                    } else {
                        System.out.println("Seu inimigo atacou mais rápido do que você e portanto você levou dando crítico");
                        this.lif -= this.enAtk * this.enDef;
                        System.out.println("Quantidade atual de vida: " + this.lif);
                    }
                }
            } else if (resposta.equalsIgnoreCase("revidar")) {
                if (acaoInimiga == 2 || acaoInimiga == 1) { // inimigo decide atacar
                    if (rdm.nextBoolean()) {
                        System.out.println("Seu inimigo tentou atacar mas você revidou com sucesso");
                        this.enLif -= this.atk * this.def;
                        System.out.println("Quantidade atual de vida: " + this.lif);
                    } else {
                        System.out.println("Você tentou reagir ao ataque inimigo mas ele atacou de forma inesperada.");
                        this.lif -= this.enAtk;
                        System.out.println("Quantidade atual de vida: " + this.lif);
                    }
                } else {
                    System.out.println("Os dois idiotas ficaram encarando um ao outro, esperado alguém atacar.");
                }
            } else {
                System.out.println("Você errou qualquer seja o que desejava fazer e perdeu -2 de sua vida");
                this.lif -= 2;
                System.out.println("Quantidade atual de vida: " + this.lif);
            }
        } while (enLif > 0 && lif > 0);

        if (lif > 0) {      // significa que o jogador venceu a luta
            // coloca os atributos inimigos de volta para o padrão
            this.enAtk = 5;
            this.enDef = 1.2f;
            this.enLif = 10;

            System.out.print("\r\nVocê venceu! +5 de dinheiro e ");
            this.din+=5;
            switch (rdm.nextInt(3)) {
                case 0:
                    System.out.print("+1 ponto de ataque! \r\n");
                    this.atk++;
                    break;
                case 1:
                    System.out.print("+0,5 ponto de defesa \r\n");
                    this.def++;
                    break;
                case 2:
                    System.out.print("+5 de vida \r\n");
                    this.lif += 5;
                    break;
            }
            return true;
        }
        else {
            this.enAtk += 1.5f;
            this.enDef += 0.7f;
            this.enLif += 5;

            System.out.println("Você perdeu!");
            return false;
        }
    }

    // loja que eventualmente aparece no jogo
    public void loja() {
        int precoAtk = (int) this.atk * 5;
        int precoDef = (int) this.def * 4;
        int precoLif = (int) this.lif * 6;
        int imortalidade = recorde * 100;

        Scanner in = new Scanner(System.in);
        System.out.println("Bem vindo ao McLabirinto! O que deseja? Itens disponíveis: \r\n Aprimorar ataque: J$" + precoAtk
                + " - Aprimorar defesa: J$" + precoDef + " Aprimorar vida: J$" + precoLif + " - IMORTALIDADE (É SÉRIO!): J$" + imortalidade + " ...ou apenas sair");
        String compra = in.next();

        if (compra.equalsIgnoreCase("ataque")) {
            if (this.din >= precoAtk) {
                System.out.println("Volte sempre!");
                this.din -= precoAtk;
                this.atk *= 3;
            } else {
                System.out.println("Você não tem dinheiro! Vaza!");
            }
        } else if (compra.equalsIgnoreCase("defesa")) {
            if (this.din >= precoDef) {
                System.out.println("Volte sempre!");
                this.din -= precoDef;
                this.def *= 2;
            } else {
                System.out.println("Não dá pra comprar nada com esse seu troco de pão! Vá embora!");
            }
        } else if (compra.equalsIgnoreCase("vida")) {
            if (this.din >= precoLif) {
                System.out.println("Volte sempre!");
                this.din -= precoLif;
                this.lif *= 4;
            } else {
                System.out.println("Dinheiro insuficiente, amigo. Adeus!");
            }
        } else if (compra.equalsIgnoreCase("imortalidade")) {
            if (this.din >= imortalidade) {
                System.out.println("Otário! Achou mesmo que seria tão fácil? Volte sempre!");
                this.din -= imortalidade;
                // Sim, a imortalidade não passa de uma pegadinha.
            } else {
                System.out.println("Acha mesmo que isso seria barato assim? Vai lá tentar ganhar algum dinheiro, vai!");
            }
        } else if (compra.equalsIgnoreCase("sair")) {
            System.out.println("Adeus, mendigo!");
            this.escolherCaminho();
        } else {
            System.out.println("Resposta inválida");
            this.loja();
        }

    }

    // aqui apenas alguns métodos talvez desnecessários e simples cujo único propósito é deixar o código mais legível
    public void avancar() {
        this.atual++;
        if (this.atual > this.recorde) {
            this.recorde++;
        }
    }

    public void encerrar() {
        this.salvar();
        System.out.println("Você alcancou a câmara: " + this.recorde + ". Parabéns!");
    }

    public String status() {
        return "Status atual do jogador: \r\nAtaque: " + this.atk + " - Defesa: " + this.def + " - Vida: "
                + this.lif + " - Câmara atual: " + this.atual + " - Recorde: " + this.recorde + " - Dinheiro: " + this.din;
    }

    public void morrer() {
        this.morreu = true;
        this.atk = 3;
        this.def = 3;
        this.lif = 15;
        this.din = 0;
        this.atual = 0;
        Scanner in = new Scanner(System.in);
        System.out.println("Deseja continuar? (sim/nao)");
        if (in.next().equalsIgnoreCase("sim")) {
            this.escolherCaminho();
        } else {
            System.out.println("Fechando...");
            System.exit(0);
        }
    }
}

