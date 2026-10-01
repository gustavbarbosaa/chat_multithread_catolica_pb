package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Servidor {
    public static final int PORTA = 3000;

    private final ServerSocket instancia;
    private final List<ConexaoCliente> clientes = Collections.synchronizedList(new ArrayList<>());

    public Servidor() throws IOException {
        this.instancia = new ServerSocket(PORTA);
        System.out.println("[SERVIDOR] Iniciado na porta " + PORTA + ".");
    }

    public void aceitarConexoes() throws IOException {
        System.out.println("[SERVIDOR] Aguardando conexões...");
        while (true) {
            try {
                Socket cliente = instancia.accept();

                ConexaoCliente conexaoCliente = new ConexaoCliente(cliente, this);

                clientes.add(conexaoCliente);
                System.out.println(
                        "[SERVIDOR] Cliente conectado: " +
                                conexaoCliente.getIdentificacao() +
                                " | ativos: " + clientes.size()
                );

                Thread thread = new Thread(conexaoCliente, "cliente-" + cliente.getPort());
                thread.start();
            } catch (IOException exception) {
                System.err.println("[SERVIDOR] Erro ao aceitar conexão: " + exception.getMessage());
            }
        }
    }

    public void distribuirMensagem(String mensagem, ConexaoCliente origem) {
        List<ConexaoCliente> destinatarios;

        synchronized (clientes) {
            destinatarios = new ArrayList<>(clientes);
        }

        destinatarios.forEach((conexaoCliente) -> {
            if (conexaoCliente != origem) {
                conexaoCliente.enviarMensagem(mensagem);
            }
        });
    }

    public void removerCliente(ConexaoCliente cliente) {
        if (clientes.remove(cliente)) {
            System.out.println(
                    "[SERVIDOR] Cliente desconectado: "
                            + cliente.getIdentificacao()
                            + " | ativos: " + clientes.size()
            );
        }
    }

}
