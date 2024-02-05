# Super CI/CD

Repositório com pipelines Jenkins para o SEI e Módulos usados internamente pela equipe do PEN.

# Organização

## Pasta Docker

Conteiner para o Jenkins com docker funcional interno e plugins já instalados.

Para buildar o jenkins e subir em seu ambiente proceda ao Readme da referida pasta


## Jenkins

Pasta com a organização de jobs e bootup jobs para subir o jenkins do zero e já pré-populado.

### bootjobs

Pasta com arquivos groovy para pré-popular o jenkins do zero. Cria credenciais (segredos) e também cria os jobs jenkins de acordo com a árvore cadastrada na pasta jobs.
Para rodar proceda com o Readme da referida pasta.

### Jobs

Pasta com a árvore de jobs. 
Cada job pode ser de um dos dois tipos:
- **.groovy:** arquivo groovy que é exatamente o job a ser executado pelo jenkins. Usado para criação de ambientes diversos por ex.
- **.jobconfig:** nesse caso o arquivo aponta para um outro job em outro repositório. O job será criado de acordo com as diretivas nesse arquivo (git, branch, credencial usada, etc) Por ex, o job para criar os ambientes do Tramita são aconselháveis estar no próprio git do tramita.
