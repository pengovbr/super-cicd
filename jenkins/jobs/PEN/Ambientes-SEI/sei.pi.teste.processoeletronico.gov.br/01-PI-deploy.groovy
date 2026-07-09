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
	        defaultValue:"4.1.5",
	        description: "Versao para o SEI, pode ser uma branch, tag ou commit. Exemplo: master, 4.0.12, v4.0.12, etc")                  
        choice(
            name: 'manterDados',
            choices:['não','sim'],
            description: "Selecione sim para não limpar os dados do banco de dados")
	    choice(
            name: 'moduloPIInstalar',
            choices: ['true', 'false'],
            description: 'Instalar Módulo Protocolo Integrado')
        string(
            name: 'moduloPIVersao',
            defaultValue:"v3.0.3",
            description: "v3.0.3 -> última versão estável usando WebService,\nv3.1.0 -> nova versão de desenvolvimento usando API REST")
        choice(
            name: 'moduloPIUrl',
            choices: ['WebService Hom: https://protocolointegrado.hom.processoeletronico.gov.br/ProtocoloWS/integradorService?wsdl', 'Rest Hom (Nova versão): https://protocolointegrado.hom.processoeletronico.gov.br/api/integracao/', 'WebService legado: https://protocolointegrado.preprod.nuvem.gov.br/ProtocoloWS/integradorService?wsdl' ],
            description: "Url para Envio das Informações")
        
    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{

                    env.JOB_URL = "sei.pi.teste.processoeletronico.gov.br"
                    env.JOB_ORGAO = "PI"
                    env.JOB_NS = "sei-pi"
                    env.MANTER_DADOS = params.manterDados

                    env.JOB_MODULOPI_URL = params.moduloPIUrl.split(': ')[1]
                    env.JOB_MODULOPI_USUARIO = params.moduloPIUsuario
                    env.JOB_MODULOPI_SENHA = params.moduloPISenha

                    env.GITSEIPAT = "github_pat_readonly_pengovbr"
                    env.GITMODULOPAT = "github_pat_readonly_pengovbr"
                    env.GITSEIDOCKERURL = "https://github.com/pengovbr/sei-docker.git"
                    env.GITSEIURL = "https://github.com/pengovbr/sei.git"

                    env.GITSEIVERSAO = params.versaoSei

                    env.MODULOPI_INSTALAR = params.moduloPIInstalar
                    env.MODULOPI_VERSAO = params.moduloPIVersao
                    env.MODULOPI_EMAIL = params.moduloPIemail
                    env.MODULOPI_URL = params.moduloPIUrl.split(': ')[1]

                    if [ "${MODULOPI_URL}" = "https://protocolointegrado.preprod.nuvem.gov.br/ProtocoloWS/integradorService?wsdl" ]; then
                        env.MODULOPI_USUARIO = "credModuloPIUsuaro"
                        env.MODULOPI_SENHA = "credModuloPISenha"
                        echo "Credenciais do Módulo PI legado selecionadas"
                    else if [ "${MODULOPI_URL}" = "https://protocolointegrado.hom.processoeletronico.gov.br/api/integracao/" ]; then
                        env.MODULOPI_USUARIO = "credModuloPIUsuarioRest"
                        env.MODULOPI_SENHA = "credModuloPIUSenhaRest"
                        echo "Credenciais do Módulo PI REST selecionadas"
                    else    
                        env.MODULOPI_USUARIO = "credModuloPIUsuarioHom"
                        env.MODULOPI_SENHA = "credModuloPIUSenhaHom"
                        echo "Credenciais do Módulo PI  legado apontando pra Homolog selecionadas"
                    fi
                    fi
                    
                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        warning('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

                }

            }
        }

        stage('Baixar SEI.php'){

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

        stage('Deletar e Subir Projeto Kubernetes'){

            steps {

                sh """
                pwd
                ls -lh

                d=sei/
                if [ -d sei/src ]; then d=sei/src; fi
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


                """

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
                    cd infra
                    echo "" >> envlocal.env
                    echo "export APP_MAIL_SERVIDOR=10.15.1.2" >> envlocal.env
                    echo "export APP_MAIL_PORTA=30025" >> envlocal.env
                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env
                    echo "export APP_HOST=${JOB_URL}" >> envlocal.env
                    echo "export APP_ORGAO=${JOB_ORGAO}" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${GITSEIVERSAO}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=${JOB_NS}" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_ANEXOS=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_DB=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_FONTES=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_SOLR=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_CONTROLADORINST=nfs-client" >> envlocal.env
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
                        echo "export PROTOCOLO_INTEGRADO_API_REST_LOGIN=${LHAVE}" >> envlocal.env

                        """
                    }

                    withCredentials([ string(credentialsId: MODULOPI_SENHA, variable: 'LHAVE')]) {

                        sh """

                        cd infra
                        echo "export MODULO_PI_SENHA=${LHAVE}" >> envlocal.env
                        echo "export PROTOCOLO_INTEGRADO_API_REST_SENHA=${LHAVE}" >> envlocal.env

                        """
                    }

                    sh """

                    cd infra

                    make kubernetes_montar_yaml

                    if [ "${MANTER_DADOS}" = "sim" ]; then
                        echo "Parametro manterDados=sim: pulando a destruição dos PVCs e dos recursos antigos"
                    else
                        make kubernetes_delete || true
                    fi

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
                            make check_isalive-timeout=1200 check-sei-isalive
                            """
                        }
                    }
                }



            }

        }

    }

}

