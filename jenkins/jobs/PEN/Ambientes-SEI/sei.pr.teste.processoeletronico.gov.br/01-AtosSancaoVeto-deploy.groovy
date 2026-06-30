/*

Job para Publicar o SEI em Cluster Kubernetes

*/

pipeline {
    agent {
        node{
            label "SEI-FONTE"
        }
    }

    parameters {

        booleanParam(
            name: 'Leiame',
            defaultValue: false,
            description: 'Atenção. A versão dos módulos ou do SEI pode ser o hash do commit; tag; branch; Antes de selecionar uma versão para os módulos verifique se o conteiner app-ci está buildado em uma data posterior ao commit que vc escolheu, caso contrário vai dar erro ao subir o ambiente. Em caso de necessidade de buildar o app-ci, caso vc não seja o dono do registry acione os donos para buildar os conteineres usando o projeto sei-docker')
        string(
            name: 'versaoSei',
            defaultValue:"main",
            description: "Branch/Tag do git para o SEI")
        booleanParam(
            name: 'detectarVersaoSei',
            defaultValue: false,
            description: 'Detectar versão do SEI baixando SEI.php da API do GitHub (api.github.com). Necessário para ambientes SEI 4.x. Para SEI 5.x deixe desmarcado — o job usa SEI 5 por padrão.')
        choice(
            name: 'moduloSancaoVetoInstalar',
            choices: ['true', 'false'],
            description: 'Instalar Módulo Sanção e Veto')
        string(
            name: 'moduloSancaoVetoVersao',
            defaultValue:"main",
            description: "Versão do Módulo Sanção e Veto")
        choice(
            name: 'moduloAtosInstalar',
            choices: ['true', 'false'],
            description: 'Instalar Módulo SEI Atos')
        string(
            name: 'moduloAtosVersao',
            defaultValue:"main",
            description: "Versão do Módulo SEI Atos")
        choice(
            name: 'multiorgao',
            choices: ['false', 'true'],
            description: 'Caso deseje criar um ambiente Multiorgao marque true e preencha corretamente os dois campos abaixo')
        string(
            name: 'multiorgaoSiglas',
            defaultValue:"SIGLA1/SIGLA2",
            description: "Depois de habilitar Multiorgao acima. Passe aqui as siglas desejadas separadas por /")
        string(
            name: 'multiorgaoNomes',
            defaultValue:"nome do orgao1/nome do orgao2",
            description: "Depois de habilitar Multiorgao acima. Passe aqui os nomes respectivos às siglas separadas por /. Informar a mesma qtde de siglas e nomes, caso contrário o parametro será ignorado na instalação")
        choice(
            name: 'federacao',
            choices: ['false', 'true'],
            description: 'Caso deseje habilitar a federação nessa instalação, informe true. Após a criação do ambiente você terá que configurar com que outro ambiente será a federação')
        choice(
            name: 'moduloEstatisticaInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Estatisticas')
        string(
            name: 'moduloEstatisticaVersao',
            defaultValue:"master",
            description: "Versao do Módulo Estatisticas")
        string(
            name: 'moduloEstatisticasUrl',
            defaultValue:"https://estatistica.dev.processoeletronico.gov.br",
            description: "Url para Envio das Informações")
        string(
            name: 'moduloEstatisticaSigla',
            defaultValue:"SEIPUBLICO",
            description: "Sigla do orgão para envio das informaçõess")
        string(
            name: 'moduloEstatisticaChave',
            defaultValue:"pass",
            description: "Chave para envio dos dados")
        choice(
            name: 'moduloPIInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Protocolo Integrado')
        string(
            name: 'moduloPIVersao',
            defaultValue:"master",
            description: "Versao do Módulo Protocolo Integrado. Vai apontar para o PI de homolog. Nome Orgao: Teste 5.0")
        string(
            name: 'moduloPIEmail',
            defaultValue:"example@example.com",
            description: "Email do Módulo do Protocolo Integrado")
        choice(
            name: 'moduloRestInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Rest')
        string(
            name: 'moduloRestVersao',
            defaultValue:"master",
            description: "Versao do Módulo Rest")
        choice(
            name: 'moduloIncomInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Incom')
        string(
            name: 'moduloIncomVersao',
            defaultValue:"master",
            description: "Versao do Módulo Incom")
        choice(
            name: 'moduloRespostaInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Resposta')
        string(
            name: 'moduloRespostaVersao',
            defaultValue:"master",
            description: "Versão do Módulo Resposta")


    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{

                    env.JOB_URL = "sei.pr.teste.processoeletronico.gov.br"
                    env.JOB_ORGAO = "PR"
                    env.JOB_NS = "sei-atos-sancao-veto"

                    env.JOB_MODULOPI_URL = "https://protocolointegrado.preprod.nuvem.gov.br/ProtocoloWS/integradorService?wsdl"
                    env.JOB_MODULOPI_USUARIO = "credModuloPIINUsuario"
                    env.JOB_MODULOPI_SENHA = "credModuloPIINSenha"

                    env.JOB_MODULOREST_URLNOTIFICACAO = "https://app-push-gestao-api.dev.nuvem.gov.br/mba-mmmessage/message"
                    env.JOB_MODULOREST_IDAPP = "4"
                    env.JOB_MODULOREST_CHAVE = "credModWsChave"
                    env.JOB_MODULOREST_TOKEN = "504CE1E9-8913-488F-AB3E-EDDABC065B06"

                    env.GITSEIPAT = "github_pat_readonly_pengovbr"
                    env.GITMODULOPAT = "github_pat_readonly_pengovbr"
                    env.GITSEIDOCKERURL = "https://github.com/pengovbr/sei-docker.git"
                    env.GITSEIURL = "https://github.com/pengovbr/sei.git"

                    env.GITSEIVERSAO = params.versaoSei

                    env.MULTIORGAO = params.multiorgao
                    env.MULTIORGAOSIGLAS = (env.MULTIORGAO == 'true' ? params.multiorgaoSiglas : "");
                    env.MULTIORGAONOMES = (env.MULTIORGAO == 'true' ? params.multiorgaoNomes : "");
                    env.FEDERACAO = params.federacao

                    env.MODULOESTATISTICA_INSTALAR = params.moduloEstatisticaInstalar
                    env.MODULOESTATISTICA_VERSAO = params.moduloEstatisticaVersao
                    env.MODULOESTATISTICA_URL = params.moduloEstatisticasUrl
                    env.MODULOESTATISTICA_SIGLA = params.moduloEstatisticaSigla
                    env.MODULOESTATISTICA_CHAVE = params.moduloEstatisticaChave

                    env.MODULOPI_INSTALAR = params.moduloPIInstalar
                    env.MODULOPI_VERSAO = params.moduloPIVersao
                    env.MODULOPI_EMAIL = params.moduloPIemail
                    env.MODULOPI_URL = env.JOB_MODULOPI_URL
                    env.MODULOPI_USUARIO = env.JOB_MODULOPI_USUARIO
                    env.MODULOPI_SENHA = env.JOB_MODULOPI_SENHA

                    env.MODULOREST_INSTALAR = params.moduloRestInstalar
                    env.MODULOREST_VERSAO = params.moduloRestVersao
                    env.MODULOREST_URLNOTIFICACAO = env.JOB_MODULOREST_URLNOTIFICACAO
                    env.MODULOREST_IDAPP = env.JOB_MODULOREST_IDAPP
                    env.MODULOREST_CHAVE = env.JOB_MODULOREST_CHAVE
                    env.MODULOREST_TOKEN = env.JOB_MODULOREST_TOKEN

                    env.MODULOINCOM_INSTALAR = params.moduloIncomInstalar
                    env.MODULOINCOM_VERSAO = params.moduloIncomVersao

                    env.MODULORESPOSTA_INSTALAR = params.moduloRespostaInstalar
                    env.MODULORESPOSTA_VERSAO = params.moduloRespostaVersao

                    env.MODULOSANCAOVETO_INSTALAR = params.moduloSancaoVetoInstalar
                    env.MODULOSANCAOVETO_VERSAO = params.moduloSancaoVetoVersao

                    env.MODULOATOS_INSTALAR = params.moduloAtosInstalar
                    env.MODULOATOS_VERSAO = params.moduloAtosVersao

                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        warning('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

                }

                sh """
                echo ${WORKSPACE}
                ls -lha

                rm -rf kube || true
                """
            }
        }

        stage('Checkout-ProjetoKube'){

            steps {

                dir('kube'){

                    sh """
                    git config --global http.sslVerify false
                    """

                    git branch: 'main',
                        url: GITSEIDOCKERURL

                    sh """
                    ls -l
                    """

                }
            }
        }

        stage('Checkout-SEI'){

            steps {

                dir('sei'){

                    sh """
                    rm -rf *
                    git config --global http.sslVerify false
                    """

                    git branch: 'main',
                        url: GITSEIURL,
                        credentialsId: GITSEIPAT

                    sh """
                    ls -l

                    git checkout ${GITSEIVERSAO}


                    """

                }
            }
        }

        stage('Deletar e Subir Projeto Kubernetes'){

            steps {
                dir('kube'){

                    withCredentials([usernamePassword(credentialsId: GITSEIPAT, usernameVariable: 'USER', passwordVariable: 'LHAVE')]) {

                        sh """
                        cd infra
                        echo "" >> envlocal.env
                        echo "export APP_FONTES_GITHUB_TOKEN=${LHAVE}" >> envlocal.env
                        """

                        sh """

                        cd infra
                        echo "" >> envlocal.env
                        echo "export GITPASS_REPO_MODULOS=${LHAVE}" >> envlocal.env

                        """

                    }
                    sh """

                    d=../sei/
                    if [ -d ../sei/src ]; then d=../sei/src; fi
                    cd \$d
                    set +e
                    grep -e "const SEI_VERSAO = '5\\..*\\..*';" sei/web/SEI.php
                    e=\$?
                    set -e
                    if [ -d ../src ]; then cd ..; fi
                    cd ../kube


                    if [ "\$e" = "0" ]; then

                        cat infra/envlocal-example-mysql-sei5.env >> infra/envlocal.env

                    fi

                    cd infra
                    echo "" >> envlocal.env
                    echo "export APP_MAIL_SERVIDOR=relay.nuvem.gov.br" >> envlocal.env
                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env
                    echo "export APP_PROTOCOLO=https" >> envlocal.env
                    echo "export APP_HOST=${JOB_URL}" >> envlocal.env
                    echo "export APP_ORGAO=${JOB_ORGAO}" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${GITSEIVERSAO}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=${JOB_NS}" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS=nfs-client2" >> envlocal.env
                    echo "export KUBERNETES_LIMITS_MEMORY_SOLR=1.5Gi" >> envlocal.env
                    echo "export KUBERNETES_LIMITS_CPU_SOLR=1000m" >> envlocal.env
                    echo "export KUBERNETES_REQUEST_MEMORY_SOLR=1.5Gi" >> envlocal.env
                    echo "export KUBERNETES_REQUEST_CPU_SOLR=1000m" >> envlocal.env
                    echo "export KUBERNETES_LIMITS_MEMORY_DB=1Gi" >> envlocal.env
                    echo "export KUBERNETES_LIMITS_CPU_DB=1000m" >> envlocal.env
                    echo "export KUBERNETES_REQUEST_MEMORY_DB=1Gi" >> envlocal.env
                    echo "export KUBERNETES_REQUEST_CPU_DB=1000m" >> envlocal.env
                    echo "export KUBERNETES_LIMITS_MEMORY_APP=1Gi" >> envlocal.env
                    echo "export KUBERNETES_LIMITS_CPU_APP=1000m" >> envlocal.env
                    echo "export KUBERNETES_REQUEST_MEMORY_APP=1Gi" >> envlocal.env
                    echo "export KUBERNETES_REQUEST_CPU_APP=1000m" >> envlocal.env

                    echo "export APP_ORGAOS_ADICIONAIS_SIGLA=${MULTIORGAOSIGLAS}" >> envlocal.env
                    echo "export APP_ORGAOS_ADICIONAIS_NOME=${MULTIORGAONOMES}" >> envlocal.env
                    echo "export APP_FEDERACAO_HABILITAR=${FEDERACAO}" >> envlocal.env

                    echo "export MODULO_ESTATISTICAS_INSTALAR=${MODULOESTATISTICA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_VERSAO=${MODULOESTATISTICA_VERSAO}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_SIGLA=${MODULOESTATISTICA_SIGLA}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_CHAVE=${MODULOESTATISTICA_CHAVE}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_URL=${MODULOESTATISTICA_URL}" >> envlocal.env

                    """


                    sh """

                    cd infra

                    echo "export MODULO_PI_INSTALAR=${MODULOPI_INSTALAR}" >> envlocal.env
                    echo "export MODULO_PI_VERSAO=${MODULOPI_VERSAO}" >> envlocal.env
                    echo "export MODULO_PI_URL=${MODULOPI_URL}" >> envlocal.env
                    echo "export MODULO_PI_EMAIL=${MODULOPI_EMAIL}" >> envlocal.env

                    """

                    withCredentials([ string(credentialsId: MODULOPI_USUARIO, variable: 'LHAVE')]) {

                        sh """

                        cd infra
                        echo "export MODULO_PI_USUARIO=${LHAVE}" >> envlocal.env

                        """
                    }

                    withCredentials([ string(credentialsId: MODULOPI_SENHA, variable: 'LHAVE')]) {

                        sh """

                        cd infra
                        echo "export MODULO_PI_SENHA=${LHAVE}" >> envlocal.env

                        """
                    }

                    sh """
                    cd infra

                    echo "export MODULO_REST_INSTALAR=${MODULOREST_INSTALAR}" >> envlocal.env
                    echo "export MODULO_REST_VERSAO=${MODULOREST_VERSAO}" >> envlocal.env
                    echo "export MODULO_REST_URL_NOTIFICACAO=${MODULOREST_URLNOTIFICACAO}" >> envlocal.env
                    echo "export MODULO_REST_ID_APP=${MODULOREST_IDAPP}" >> envlocal.env
                    echo "export MODULO_REST_TOKEN_SECRET=${MODULOREST_TOKEN}" >> envlocal.env
                    """

                    withCredentials([ string(credentialsId: MODULOREST_CHAVE, variable: 'LHAVE')]) {

                        sh """

                        cd infra
                        echo "export MODULO_REST_CHAVE=${LHAVE}" >> envlocal.env

                        """
                    }

                    sh """
                    cd infra


                    echo "export MODULO_INCOM_INSTALAR=${MODULOINCOM_INSTALAR}" >> envlocal.env
                    echo "export MODULO_INCOM_VERSAO=${MODULOINCOM_VERSAO}" >> envlocal.env

                    echo "export MODULO_RESPOSTA_INSTALAR=${MODULORESPOSTA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_RESPOSTA_VERSAO=${MODULORESPOSTA_VERSAO}" >> envlocal.env

                    echo "export MODULO_SANCAOVETO_INSTALAR=${MODULOSANCAOVETO_INSTALAR}" >> envlocal.env
                    echo "export MODULO_SANCAOVETO_VERSAO=${MODULOSANCAOVETO_VERSAO}" >> envlocal.env

                    echo "export MODULO_ATOS_INSTALAR=${MODULOATOS_INSTALAR}" >> envlocal.env
                    echo "export MODULO_ATOS_VERSAO=${MODULOATOS_VERSAO}" >> envlocal.env

                    """

                    sh """

                    cd infra

                    echo "export KUBERNETES_PVC_STORAGECLASS=nfs-client" >> envlocal.env

                    make kubernetes_montar_yaml
                    make kubernetes_delete || true

                    make kubernetes_montar_yaml
                    make kubernetes_apply

                    sleep 20
                    kubectl -n ${JOB_NS} scale --replicas=0 deployment/jod || true
                    """


                }

            }
        }

        stage('Rodando Atualizador'){

            steps {

                dir('kube'){

                    sh """

                    sleep 60
                    i=0
                    while true; do

                      echo "Vamos printar o log dessa execucao, atencao que pode haver mais logs acima de outras tentativas..."
                      set +e
                      kubectl -n ${JOB_NS} logs -f job.batch/sei-inicializador
                      set -e

                      if kubectl -n ${JOB_NS} wait --for=condition=complete job/sei-inicializador --timeout=0 2>/dev/null; then
                        job_result=0
                        break
                      fi

                      if kubectl -n ${JOB_NS} wait --for=condition=failed job/sei-inicializador --timeout=0 2>/dev/null; then
                        job_result=1
                        break
                      fi

                      sleep 60


                    done

                    if [[ \$job_result -eq 1 ]]; then
                        echo "Job failed!"
                        exit 1
                    fi

                    """

                }
            }
        }

        stage('Verificando Componentes Kube'){

            parallel {

                stage('Database'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            make KUBE_DEPLOY_NAME=db kubernetes_check_deploy_generic
                            """
                        }
                    }
                }


                stage('JOD'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            make KUBE_DEPLOY_NAME=jod kubernetes_check_deploy_generic
                            """
                        }
                    }
                }


                stage('Memcached'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            make KUBE_DEPLOY_NAME=memcached kubernetes_check_deploy_generic
                            """
                        }
                    }
                }


                stage('Solr'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            make KUBE_DEPLOY_NAME=solr kubernetes_check_deploy_generic
                            """
                        }
                    }
                }


                stage('APP'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            make kube_timeout=1200s KUBE_DEPLOY_NAME=sei-app kubernetes_check_deploy_generic
                            """
                        }
                    }
                }


                stage('URL Respondendo'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            echo "export APP_HOST=${JOB_URL}" >> envlocal.env
                            make check_isalive-timeout=300 check-sei-isalive
                            """
                        }
                    }
                }



            }

        }


    }
}
