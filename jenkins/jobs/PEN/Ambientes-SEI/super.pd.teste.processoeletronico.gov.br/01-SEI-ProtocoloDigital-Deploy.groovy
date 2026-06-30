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
          choice(
              name: 'servicoProtocoloDigitalInstalar',
              choices: ['true', 'false'],
              description: 'Habilitar nesse ambiente o Protocolo Digital')
          string(
              name: 'servicoProtocoloDigitalSigla',
              defaultValue:"GOV.BR",
              description: "Sigla para o Sistema PD")
          string(
              name: 'servicoProtocoloDigitalNome',
              defaultValue:"Protocolo.GOV.BR",
              description: "Nome para o Sistema PD")
          string(
              name: 'servicoProtocoloDigitalOperacoes',
              defaultValue:"3,2,15,0,1",
              description: "Id das operacoes para o PD")
        choice(
            name: 'moduloRespostaInstalar',
            choices: ['true', 'false'],
            description: 'Instalar Módulo Resposta')
          string(
              name: 'moduloRespostaVersao',
              defaultValue:"master",
              description: "Versão do Módulo Resposta")
          string(
              name: 'moduloRespostaSistemaId',
              defaultValue:'a:1:{i:0;s:1:"8";}',
              description: "Id do Sistema a ser Vinculado, coloque vazio caso n deseje vincular nada")
          string(
              name: 'moduloRespostaDocumentoId',
              defaultValue:'153',
              description: "Id do Documento a ser Vinculado")
    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{
                    env.GITSEIPAT = "github_pat_readonly_pengovbr"
                    env.GITMODULOPAT = "github_pat_readonly_pengovbr"
                    env.GITSEIDOCKERURL = "https://github.com/pengovbr/sei-docker.git"
                    env.GITSEIURL = "https://github.com/pengovbr/sei.git"

                    env.GITSEIVERSAO = params.versaoSei
                    env.SERVICOPD_INSTALAR = params.servicoProtocoloDigitalInstalar
                    env.SERVICOPD_SIGLA = params.servicoProtocoloDigitalSigla
                    env.SERVICOPD_NOME = params.servicoProtocoloDigitalNome
                    env.SERVICOPD_OPERACOES = params.servicoProtocoloDigitalOperacoes

                    env.MODULORESPOSTA_INSTALAR = params.moduloRespostaInstalar
                    env.MODULORESPOSTA_VERSAO = params.moduloRespostaVersao
                    env.MODULORESPOSTA_SISTEMA_ID = params.moduloRespostaSistemaId
                    env.MODULORESPOSTA_DOCUMENTO_ID = params.moduloRespostaDocumentoId

                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        error 'Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente'
                    }

                }

                sh """
                echo ${WORKSPACE}
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
                    echo "export APP_HOST=super.pd.teste.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=git@github.com:pengovbr/sei" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${GITSEIVERSAO}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=sei-pd" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS=nfs-client" >> envlocal.env
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

                    echo "export SERVICO_PD_INSTALAR=${SERVICOPD_INSTALAR}" >> envlocal.env
                    echo "export SERVICO_PD_SIGLA=${SERVICOPD_SIGLA}" >> envlocal.env
                    echo "export SERVICO_PD_NOME=${SERVICOPD_NOME}" >> envlocal.env
                    echo "export SERVICO_PD_OPERACOES=${SERVICOPD_OPERACOES}" >> envlocal.env

                    echo "export MODULO_RESPOSTA_INSTALAR=${MODULORESPOSTA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_RESPOSTA_VERSAO=${MODULORESPOSTA_VERSAO}" >> envlocal.env
                    echo "export MODULO_RESPOSTA_SISTEMA_ID=${MODULORESPOSTA_SISTEMA_ID}" >> envlocal.env
                    echo "export MODULO_RESPOSTA_DOCUMENTO_ID=${MODULORESPOSTA_DOCUMENTO_ID}" >> envlocal.env

                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env

                    make kubernetes_montar_yaml
                    make kubernetes_delete || true

                    make kubernetes_montar_yaml
                    #make kubernetes_apply
                    cd orquestrators/rancher-kubernetes/topublish/; \
                    kubectl --insecure-skip-tls-verify apply -f configmaps.yaml; \
                    kubectl --insecure-skip-tls-verify apply -f secrets.yaml; \
                    sleep 2
                    kubectl --insecure-skip-tls-verify apply -f pvc.yaml; \
                    kubectl --insecure-skip-tls-verify apply -f jobs.yaml; \
                    sleep 2
                    kubectl --insecure-skip-tls-verify apply -f statefullsets.yaml; \
                    kubectl --insecure-skip-tls-verify apply -f deploys-svc.yaml; \
                    sleep 10
                    kubectl --insecure-skip-tls-verify apply -f ingress.yaml;

                    sleep 20
                    kubectl --insecure-skip-tls-verify -n sei-pd scale --replicas=0 deployment/jod || true
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
                            make kube_timeout=900s KUBE_DEPLOY_NAME=sei-app kubernetes_check_deploy_generic
                            """
                        }
                    }
                }


                stage('URL Respondendo'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            echo "export APP_HOST=super.pd.teste.processoeletronico.gov.br" >> envlocal.env
                            make check_isalive-timeout=180 check-sei-isalive
                            """
                        }
                    }
                }



            }

        }


    }

}
