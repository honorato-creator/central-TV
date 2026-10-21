# Central TV

Painel da Central Kaio Honorato rodando na TV Box MXQ Pro 4K como se fosse o sistema da box: ligou na tomada, abre o painel.

- `painel/` → o painel (a Netlify publica esta pasta em https://central-kaio.netlify.app)
- `app/` → o app Android **Central TV**, que vira a tela inicial da box
- A cada mudança no `app/`, o GitHub gera o `CentralTV.apk` em **Releases**

## Preparar a box (uma vez)

1. **Zerar**: Configurações → Armazenamento/Sobre → **Redefinir para configuração original** (ou botão de reset no buraco do AV ao ligar → *Wipe data/factory reset*).
2. Conectar no Wi-Fi.
3. **Permitir instalar APK**: Configurações → Segurança → **Fontes desconhecidas** → ligar (no Android 9 a permissão é pedida para o gerenciador de arquivos na hora de instalar).
4. Baixar o `CentralTV.apk` da última versão em **Releases** num pen drive, plugar na box e abrir pelo gerenciador de arquivos → **Instalar**.
5. Apertar **Home** no controle → escolher **Central TV** → **Sempre**.
6. Abrir o painel, entrar com o e-mail e a senha do painel. A box guarda o login.
7. Opcional, para liberar memória: Configurações → Apps → desativar ou desinstalar Netflix, YouTube, Kodi e o que vier de fábrica.
8. Se o painel abrir desalinhado: atualizar **Android System WebView** (Play Store ou APK) antes de desativar a Play Store.

Teste final: tire da tomada e ligue de novo. Deve abrir direto no painel.

## Manutenção
No controle, aperte **↑ ↑ ↓ ↓ ← → ← →** em até 4 segundos entre as teclas: abre as configurações do Android.
O app recarrega o painel a cada 6 horas para pegar versões novas. Sem internet, mostra "Sem conexão" e tenta de novo a cada 15 s.

## Atualizar o app
Mudou algo em `app/` → o GitHub gera uma versão nova em Releases → instalar por cima (mesma chave de assinatura, não precisa desinstalar).
