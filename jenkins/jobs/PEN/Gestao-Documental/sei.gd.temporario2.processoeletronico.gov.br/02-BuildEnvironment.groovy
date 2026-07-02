/*
Usuario jenkins precisa ter permissao de sudo
Jenkins minimo em 2.332
Criar secrets de acordo com os parameters informados no job
Precisa de um noh com label temporario1, ou altere abaixo de acordo com seu cluster
O que esse job faz:
- Baixa o codigo do sei
- Baixa o projeto sei-docker
- Vai no sei-docker e configura para a url sei.gd.temporario1.processoeletronico.gov.br (altere p suas necessidades)
- Configura tb para instalar o GD na versao informada
- Sobe o projeto no sei-docker e aguarda entrar no ar

PS: ele usa o sei-docker para rodar o modulo, portanto verifique se a data do build do conteiner app-ci tem a versao desejada do GD
*/

pipeline {

    agent {
        node{
            label "AmbienteGD2"
        }
    }

    parameters {
        string(
            name: 'versaoSei',
            defaultValue: 'main',
            description: 'Versao do Sei')
        booleanParam(name: 'bolInstalarModulo',
            defaultValue: true,
            description: 'Marque/desmarque para instalar o módulo GD')
        string(
            name: 'versaoGestaoDocumental',
            defaultValue: '1.2.7',
            description: 'Caso a opção acima esteja marcada informe uma versão válida')
        choice(
            name: 'database',
            choices: "mysql\noracle\nsqlserver",
            description: 'Qual o banco de dados' )
        booleanParam(
            name: 'bolLimparConteiners',
            defaultValue: false,
            description: 'Marque para remover conteineres e volumes antes de subir o ambiente')
        choice(
            name: 'choiceAviso',
            choices: "Não Ignorar Aviso\nIgnorar Aviso",
            description: 'Mostrar Aviso de checagem antes de rodar' )

    }

    stages {

        stage('Checkout Sei e sei-docker'){

            steps {

                script{

                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        error('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

                    env.DIRECTORY = WORKSPACE
                    env.VERSAO_SEI = params.versaoSei
                    env.BOLINSTALARMODULO = params.bolInstalarModulo
                    env.VERSAO_GD = params.versaoGestaoDocumental
                    env.DATABASE = params.database
                    env.GITURL = "https://github.com/pengovbr/sei.git"
                    env.GITCRED = "github_pat_readonly_pengovbr"
                    env.GITBRANCH = "main"
                    env.FOLDERSEI = "/home/jenkins/foldersei"
                    env.GITURLSEIDOCKER = "https://github.com/spbgovbr/sei-docker"
                    env.GITCREDSEIDOCKER = "github_pat_readonly_pengovbr"
                    env.GITBRANCHSEIDOCKER = "main"
                    env.FOLDERSEIDOCKER = "/home/jenkins/folderseidocker"
                    env.BOLLIMPARCONTEINERES = params.bolLimparConteiners
                    env.IGNORARAVISO = params.choiceAviso

                    if(env.IGNORARAVISO != 'Ignorar Aviso'){
                        msg = "ATENÇÃO. Antes de continuar, verifique o seguinte:\n"
                        msg = msg + "- RODE ANTES O JOB PARA ATUALIZAR A DATA DO AMBIENTE PARA O MOMENTO ESPERADO\n"
                        msg = msg + "- veja se não há outros jobs referentes ao GD rodando\n"
                        msg = msg + "- Caso exista espere a finalização ou encerre-os. \n"
                        r = input message: msg, ok: 'Já olhei. Manda ver!'
                    }

                    sh """
                    mkdir -p ${FOLDERSEIDOCKER}/infra
                    """

                    dir("${FOLDERSEIDOCKER}/infra"){

                        sh """
                        make clear || true
                        make apagar_volumes || true
                        """
                    }

                    sh """
                    rm -rf ${FOLDERSEIDOCKER} || true
                    rm -rf ${FOLDERSEI} || true
                    """


                    dir("${FOLDERSEI}"){

                        sh """
                        git config --global http.sslVerify false
                        """

                        git branch: GITBRANCH,
                            credentialsId: GITCRED,
                            url: GITURL

                        sh """
                        git checkout ${VERSAO_SEI}
                        ls -l

                        mkdir -p src
                        \\cp -R infra sei sip src || true
                        """

                    }

                    dir("${FOLDERSEIDOCKER}"){

                        sh """
                        git config --global http.sslVerify false
                        """

                        git branch: GITBRANCHSEIDOCKER,
                            credentialsId: GITCREDSEIDOCKER,
                            url: GITURLSEIDOCKER

                        sh """
                        ls -l
                        """

                    }
                }

            }

        }

        stage('Limpar Conteineres/Volumes'){
            when {
                expression { BOLLIMPARCONTEINERES }
            }
            steps{
                script{
                    sh """
                    docker stop \$(docker ps -aq) || true
                    docker rm \$(docker ps -aq) || true
                    docker volume prune -f || true
                    """
                }
            }
        }

        stage("Subir Ambiente"){

            steps{
                script{

                    dir("${FOLDERSEIDOCKER}/infra"){

                        sh """
                        make clear || true
                        make apagar_volumes || true

                        versao=\$(grep -o -E '[0-9]{1}\\.[0-9]{1,2}\\.[0-9]{1,2}' ${FOLDERSEI}/src/sei/web/SEI.php | head -1 | head -c 1)

                        rm -rf envlocal.env
                        \\cp envlocal-example-mysql-sei4.env envlocal.env
                        \\cat envlocal-example-${DATABASE}-sei\${versao}.env >> envlocal.env

                        sed -i "s|LOCALIZACAO_FONTES_SEI=.*|LOCALIZACAO_FONTES_SEI=${FOLDERSEI}/src|g" envlocal.env
                        sed -i "s|export APP_PROTOCOLO=.*|export APP_PROTOCOLO=http|g" envlocal.env
                        sed -i "s|export APP_HOST=.*|export APP_HOST=sei.gd.temporario2.processoeletronico.gov.br|g" envlocal.env

                        sed -i "s|MODULO_GESTAODOCUMENTAL_VERSAO=.*|MODULO_GESTAODOCUMENTAL_VERSAO=${VERSAO_GD}|g" envlocal.env
                        echo "export JOD_PRESENTE=false" >> envlocal.env
                        echo "export APP_PORTA_80_MAP_EXPOR=true" >> envlocal.env
                        echo "export BALANCEADOR_PRESENTE=false" >> envlocal.env
                        echo "export APP_MAIL_SERVIDOR=10.15.1.2" >> envlocal.env
                        echo "export APP_MAIL_PORTA=30025" >> envlocal.env

                        echo "export APP_FONTES_GIT_CHECKOUT=${GITBRANCH}" >> envlocal.env

                        """

                        withCredentials([usernamePassword(credentialsId: GITCRED, usernameVariable: 'USER', passwordVariable: 'LHAVE')]) {

                            sh """

                            echo "" >> envlocal.env

                            echo "" >> envlocal.env
                            echo "export GITPASS_REPO_MODULOS=${LHAVE}" >> envlocal.env

                            """

                        }


                        if(BOLINSTALARMODULO){

                            sh """
                            sed -i "s|export MODULO_GESTAODOCUMENTAL_INSTALAR=.*|export MODULO_GESTAODOCUMENTAL_INSTALAR=true|g" envlocal.env
                            """

                        }else{

                            sh """
                            sed -i "s|export MODULO_GESTAODOCUMENTAL_INSTALAR=.*|export MODULO_GESTAODOCUMENTAL_INSTALAR=false|g" envlocal.env
                            """

                        }

                        sh """
                        make setup

                        docker stop docker-compose_app-agendador_1 docker-compose-app-agendador-1  || true
                        docker rm docker-compose_app-agendador_1 docker-compose-app-agendador-1  || true
                        """

                    }

                }
            }
        }

        stage('Executando Instalador'){

            steps {

                dir("${FOLDERSEIDOCKER}/infra"){

                    sh """

                    sleep 30
                    i=0
                    while true; do

                      echo "Vamos printar o log dessa execucao, atencao que pode haver mais logs acima de outras tentativas..."
                      set +e
                      docker logs -f docker-compose-app-atualizador-1
                      set -e

                      if \$(docker logs -f docker-compose-app-atualizador-1 | grep "Atualizador chegou ao final..." >/dev/null); then
                        job_result=0
                        break
                      else
                        job_result=1
                        break
                      fi

                      sleep 10

                    done

                    if [[ \$job_result -eq 1 ]]; then
                        echo "Job failed!"
                        exit 1
                    fi

                    """

                }
            }
        }

        stage('Wait Environments Wakeup'){
            steps {
                script {
                    timeout(time: 3, unit: 'MINUTES') {
                        sh script: """
                        set +e

                        resultado=1;
                        while [ ! \$resultado -eq 0 ];
                        do
                           sleep 5;
                           echo "Ainda nao esta pronto, Tentando acessar novamente...";

                           curl -sL --head --resolve "sei.gd.temporario2.processoeletronico.gov.br:80:127.0.0.1" --request GET sei.gd.temporario2.processoeletronico.gov.br/sei/ | grep "200 OK"
                           resultado=\$?;
                        done
                        """, label: "Testando se url online"
                    }
                }

            }
        }
    }
}