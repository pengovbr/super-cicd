# Pasta bootjobs

## createCredentials.groovy

Esse script vai criar as credenciais de acordo com os valores informados na variável "secrets".
Cada secret pode ser de um dos seguintes tipos:
- usernamepass
- secret text
- secret file
- ssh chave privada

Para verificar os separadores e também um exemplo de como passar as credenciais basta visualizar no próprio script o exemplo.

### Como rodar

- prepare o script createCredentials.groovy. Copie para um bloco de notas e substitua o conteúdo da variável secrets pelas suas credenciais obedecendo o template. Ver template no proprio script, lá tem um exemplo.

- No jenkins crie um pipeline com nome temporario

- desmarque a caixa "use groovy sandbox"

- salve no campo script o conteúdo do arquivo script createCredentials.groovy trabalhado

- salve o job

- como é um script que vai criar objetos no jenkins, será necessário acessar a interface de administração do jenkins e liberar a permissão desse script (manage jenkins -> In-process Script Approval)

- execute o job e as credenciais serão criadas. Acompanhe a execução do job pelo log do job no jenkins

- não esqueça de apagar o job para evitar acessos indesejados

## createJobs.groovy

Esse job vai popular o jenkins com todos os jobs necessários. Para tanto será usado a árvore disponível na pasta super-cicd/jenkins/jobs deste repositório.

Cada job pode ser de um dos dois tipos:
- **.groovy:** arquivo groovy que é exatamente o job a ser executado pelo jenkins. Usado para criação de ambientes diversos por ex.
- **.jobconfig:** nesse caso o arquivo aponta para um outro job em outro repositório. O job será criado de acordo com as diretivas nesse arquivo (git, branch, credencial usada, etc) Por ex, o job para criar os ambientes do Tramita são aconselháveis estar no próprio git do tramita.

### Como rodar

- No jenkins crie um pipeline com nome temporario

- desmarque a caixa "use groovy sandbox"

- salve no campo script o conteúdo do arquivo script createJobs.groovy 
- nesse script altere o valor da variável git url, se for o caso para usar o seu repo

- salve o job

- como é um script que vai criar objetos no jenkins, será necessário acessar a interface de administração do jenkins e liberar a permissão desse script (manage jenkins -> In-process Script Approval)

- execute o job e a árvore de jobs será espelhda de acordo com o repositório. Acompanhe a execução do job pelo log do job no jenkins

- não esqueça de apagar o job para evitar acessos indesejados