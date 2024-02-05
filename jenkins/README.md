# Pasta Jenkins

Pasta com a organização de jobs e bootup jobs para subir o jenkins do zero e já pré-populado.
Uma vez que o Jenkins esteja rodadno e acessível proceda com os bootjobs abaixo para pré-popular o mesmo.

## bootjobs

Pasta com arquivos groovy para pré-popular o jenkins do zero. Cria credenciais (segredos) e também cria os jobs jenkins de acordo com a árvore cadastrada na pasta jobs.
Para rodar proceda com o Readme da referida pasta.

## Jobs

Pasta com a árvore de jobs. 
Cada job pode ser de um dos dois tipos:
- **.groovy:** arquivo groovy que é exatamente o job a ser executado pelo jenkins. Usado para criação de ambientes diversos por ex.
- **.jobconfig:** nesse caso o arquivo aponta para um outro job em outro repositório. O job será criado de acordo com as diretivas nesse arquivo (git, branch, credencial usada, etc) Por ex, o job para criar os ambientes do Tramita são aconselháveis estar no próprio git do tramita.

Essa pasta deverá seguir esse padrão pois um dos bootjobs para criar a árvore de jobs utilizará a mesma.
