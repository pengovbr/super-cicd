/*

Job para Publicar o SEI em Cluster Kubernetes

*/

pipeline {

    agent any

    parameters {

        booleanParam(
            name: 'Leiame',
            defaultValue: false,
            description: 'Atenção. A versão dos módulos ou do SEI pode ser o hash do commit; tag; branch;')
        string(
	        name: 'versaoSei',
	        defaultValue:"main",
	        description: "Branch/Tag do git para o SEI")

        choice(
            name: 'moduloAssinaturaInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Assinatura Avançada')
	    string(
	        name: 'moduloAssinaturaVersao',
	        defaultValue:"master",
	        description: "Versão do Módulo Assinatura Avançada")
        choice(
            name: 'moduloAssinaturaCredencial',
            choices: [ 'Homolog' ],
            description: 'Selecione a credencial para uso no ambiente')

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

                    env.MODULOLOGINUNICO_INSTALAR = params.moduloLoginUnicoInstalar
                    env.MODULOLOGINUNICO_VERSAO = params.moduloLoginUnicoVersao
                    env.MODULOLOGINUNICO_ORGAO = params.moduloLoginUnicoOrgao

                    env.MODULOLOGINUNICO_CLIENTID = ""
                    env.MODULOLOGINUNICO_SECRET = ""
                    env.MODULOLOGINUNICO_URLPROVIDER = ""
                    env.MODULOLOGINUNICO_REDIRECTURL = ""
                    env.MODULOLOGINUNICO_URLLOGOUT = ""
                    env.MODULOLOGINUNICO_SCOPE = ""
                    env.MODULOLOGINUNICO_SERVICOS = ""
                    env.MODULOLOGINUNICO_REVALIDACAO = ""
                    env.MODULOLOGINUNICO_CLIENTIDVALIDACAO = ""
                    env.MODULOLOGINUNICO_SECRETVALIDACAO = ""
                    env.MODULOLOGINUNICO_ORGAO = ""

                    env.MODULOASSINATURA_INSTALAR = params.moduloAssinaturaInstalar
                    env.MODULOASSINATURA_VERSAO = params.moduloAssinaturaVersao

                    env.MODULOASSINATURA_CREDENCIAL = "modAssinatura-" + params.moduloAssinaturaCredencial + ".env"

                    env.MODULOPETICIONAMENTO_INSTALAR = params.moduloPeticionamentoInstalar
                    env.MODULOPETICIONAMENTO_VERSAO = params.moduloPeticionamentoVersao
                    env.MODULOPETICIONAMENTO_URL = params.moduloPeticionamentoURL



                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        warning('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

                }

                sh """
                echo ${WORKSPACE}
                ls -lha

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

                    withCredentials([file(credentialsId: MODULOASSINATURA_CREDENCIAL, variable: 'ENVASSIN')]) {
                        sh """
                        pwd
                        ls -lha
                        ls -lha ../
                        cat ${ENVASSIN} > Assinatura.env
                        """
                    }

                    script {

                        def props = readProperties file: 'Assinatura.env'

                        env.MODULO_ASSINATURA_URLPROVIDER = props['MODULO_ASSINATURA_URLPROVIDER']
                        env.MODULO_ASSINATURA_CLIENTID = props['MODULO_ASSINATURA_CLIENTID']
                        env.MODULO_ASSINATURA_SECRET = props['MODULO_ASSINATURA_SECRET']

                        env.MODULO_ASSINATURA_VALIDAR_API_URL = props['MODULO_ASSINATURA_VALIDAR_API_URL']
                        env.MODULO_ASSINATURA_VALIDAR_API_KEY = props['MODULO_ASSINATURA_VALIDAR_API_KEY']

                        env.MODULO_ASSINATURA_SUITE = props['MODULO_ASSINATURA_SUITE']

                        env.MODULO_ASSINATURA_PKCS12_URL = props['MODULO_ASSINATURA_PKCS12_URL']
                        env.MODULO_ASSINATURA_PKCS12_URL_ASSINAR = props['MODULO_ASSINATURA_PKCS12_URL_ASSINAR']

                        env.MODULO_ASSINATURA_YKUE_URL = props['MODULO_ASSINATURA_YKUE_URL']
                        env.MODULO_ASSINATURA_YKUE_URL_ASSINAR = props['MODULO_ASSINATURA_YKUE_URL_ASSINAR']

                        env.MODULO_ASSINATURA_INTEGRA_ICP_URL = props['MODULO_ASSINATURA_INTEGRA_ICP_URL']
                        env.MODULO_ASSINATURA_INTEGRA_ICP_URL_CLEARINGS = props['MODULO_ASSINATURA_INTEGRA_ICP_URL_CLEARINGS']
                        env.MODULO_ASSINATURA_INTEGRA_ICP_URL_ASSINAR = props['MODULO_ASSINATURA_INTEGRA_ICP_URL_ASSINAR']

                        env.MODULO_ASSINATURA_CLOUDPSC_URL = props['MODULO_ASSINATURA_CLOUDPSC_URL']
                        env.MODULO_ASSINATURA_CLOUDPSC_URL_START = props['MODULO_ASSINATURA_CLOUDPSC_URL_START']
                        env.MODULO_ASSINATURA_CLOUDPSC_URL_ASSINAR = props['MODULO_ASSINATURA_CLOUDPSC_URL_ASSINAR']

                        env.MODULO_ASSINATURA_API_KEY_ITYHY = props['MODULO_ASSINATURA_API_KEY_ITYHY']


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
                    echo "export APP_HOST=sei.assinatura.teste.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=git@github.com:pengovbr/sei" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${GITSEIVERSAO}" >> envlocal.env
                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env
                    echo "export APP_MAIL_SERVIDOR=relay.nuvem.gov.br" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=sei-assinatura" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_ANEXOS=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_DB=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_FONTES=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_SOLR=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS_CONTROLADORINST=nfs-client" >> envlocal.env
                    echo "export KUBERNETES_INGRESS_PROXY_CONNECT_TIMEOUT=300" >> envlocal.env
                    echo "export KUBERNETES_INGRESS_PROXY_READ_TIMEOUT=300" >> envlocal.env
                    echo "export KUBERNETES_INGRESS_PROXY_SEND_TIMEOUT=300" >> envlocal.env
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

                    echo "export MODULO_LOGINUNICO_INSTALAR=${MODULOLOGINUNICO_INSTALAR}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_VERSAO=${MODULOLOGINUNICO_VERSAO}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_URLPROVIDER=${MODULOLOGINUNICO_URLPROVIDER}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_REDIRECTURL=${MODULOLOGINUNICO_REDIRECTURL}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_URLLOGOUT=${MODULOLOGINUNICO_URLLOGOUT}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_SCOPE=${MODULOLOGINUNICO_SCOPE}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_URLSERVICOS=${MODULOLOGINUNICO_SERVICOS}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_URLREVALIDACAO=${MODULOLOGINUNICO_REVALIDACAO}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_CIENTIDVALIDACAO=${MODULOLOGINUNICO_CLIENTIDVALIDACAO}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_ORGAO=${MODULOLOGINUNICO_ORGAO}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_INSTALAR=${MODULOASSINATURA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_VERSAO=${MODULOASSINATURA_VERSAO}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_URLPROVIDER=${MODULO_ASSINATURA_URLPROVIDER}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_CLIENTID=${MODULO_ASSINATURA_CLIENTID}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_SECRET=${MODULO_ASSINATURA_SECRET}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_VALIDAR_API_URL=${MODULO_ASSINATURA_VALIDAR_API_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_VALIDAR_API_KEY=${MODULO_ASSINATURA_VALIDAR_API_KEY}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_SUITE=${MODULO_ASSINATURA_SUITE}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_PKCS12_URL=${MODULO_ASSINATURA_PKCS12_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_PKCS12_URL_ASSINAR=${MODULO_ASSINATURA_PKCS12_URL_ASSINAR}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_YKUE_URL=${MODULO_ASSINATURA_YKUE_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_YKUE_URL_ASSINAR=${MODULO_ASSINATURA_YKUE_URL_ASSINAR}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_INTEGRA_ICP_URL=${MODULO_ASSINATURA_INTEGRA_ICP_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_INTEGRA_ICP_URL_CLEARINGS=${MODULO_ASSINATURA_INTEGRA_ICP_URL_CLEARINGS}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_INTEGRA_ICP_URL_ASSINAR=${MODULO_ASSINATURA_INTEGRA_ICP_URL_ASSINAR}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_CLOUDPSC_URL=${MODULO_ASSINATURA_CLOUDPSC_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_CLOUDPSC_URL_START=${MODULO_ASSINATURA_CLOUDPSC_URL_START}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_CLOUDPSC_URL_ASSINAR=${MODULO_ASSINATURA_CLOUDPSC_URL_ASSINAR}" >> envlocal.env

                    echo "export MODULO_ASSINATURA_API_KEY_ITYHY=${MODULO_ASSINATURA_API_KEY_ITYHY}" >> envlocal.env


                    echo "export MODULO_PETICIONAMENTO_INSTALAR=${MODULOPETICIONAMENTO_INSTALAR}" >> envlocal.env
                    echo "export MODULO_PETICIONAMENTO_VERSAO=${MODULOPETICIONAMENTO_VERSAO}" >> envlocal.env
                    echo "export MODULO_PETICIONAMENTO_URL=${MODULOPETICIONAMENTO_URL}" >> envlocal.env

                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env

                    make kubernetes_montar_yaml
                    make kubernetes_delete || true

                    make kubernetes_montar_yaml
                    make kubernetes_apply
                    #cd orquestrators/rancher-kubernetes/topublish/; \
                    #kubectl --insecure-skip-tls-verify apply -f configmaps.yaml; \
                    #kubectl --insecure-skip-tls-verify apply -f secrets.yaml; \
                    #sleep 2
                    #kubectl --insecure-skip-tls-verify apply -f pvc.yaml; \
                    #kubectl --insecure-skip-tls-verify apply -f jobs.yaml; \
                    #sleep 2
                    #kubectl --insecure-skip-tls-verify apply -f statefullsets.yaml; \
                    #kubectl --insecure-skip-tls-verify apply -f deploys-svc.yaml; \
                    #sleep 10
                    #kubectl --insecure-skip-tls-verify apply -f ingress.yaml;

                    sleep 20
                    kubectl --insecure-skip-tls-verify -n sei-assinatura scale --replicas=0 deployment/jod || true
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


            }

        }

        stage('Solr and URL'){
            parallel {

                stage('Create Solr Core'){

                    steps {

                        dir('solr'){

                            sh """
                            rm -rf *
                            rm -rf .* || true

                            PODSOLR=\$(kubectl get pod -n sei-assinatura -l servico=solr | tail -1 | cut -d " " -f 1)
                            PODAPP=\$(kubectl get pod -n sei-assinatura -l servico=app | tail -1 | cut -d " " -f 1)

                            kubectl cp sei-assinatura/\${PODAPP}:/sei-modulos/mod-sei-assinatura-eletronica .

                            kubectl -n sei-assinatura exec -it \${PODSOLR} -- mkdir -p /opt/solr/server/solr/mod-sei-assinatura

                            kubectl cp solr sei-assinatura/\${PODSOLR}:/opt/solr/server/solr/mod-sei-assinatura/conf

                            kubectl -n sei-assinatura exec -it \${PODSOLR} -- chown -R solr:solr /opt/solr/server/solr/mod-sei-assinatura

                            kubectl -n sei-assinatura exec -it \${PODSOLR} -- chmod -R u+rwX /opt/solr/server/solr/mod-sei-assinatura
                            kubectl -n sei-assinatura exec -it \${PODSOLR} -- bash -c 'SOLR_AUTH_TYPE="basic" SOLR_AUTHENTICATION_OPTS="-Dbasicauth=admin:SolrAdmin123\$" /opt/solr/bin/solr create -c mod-sei-assinatura -d /opt/solr/server/solr/mod-sei-assinatura/conf'

                            """
                        }
                    }
                }

                stage('URL Respondendo'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            echo "export APP_HOST=sei.assinatura.teste.processoeletronico.gov.br" >> envlocal.env
                            make check_isalive-timeout=180 check-sei-isalive
                            """
                        }
                    }
                }
            }
        }

    }
}
