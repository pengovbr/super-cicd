/*

Job para Publicar o SUPER em Cluster Kubernetes

*/

pipeline {
    agent {
        node{
            label "SUPER-FONTE"
        }
    }

    parameters {

        booleanParam(
            name: 'Leiame',
            defaultValue: false,
            description: 'Atenção. A versão dos módulos ou do SUPER pode ser o hash do commit; tag; branch; Antes de selecionar uma versão para os módulos verifique se o conteiner app-ci está buildado em uma data posterior ao commit que vc escolheu, caso contrário vai dar erro ao subir o ambiente. Em caso de necessidade de buildar o app-ci, caso vc não seja o dono do registry acione os donos para buildar os conteineres usando o projeto super-docker')

	      string(
	          name: 'versaoSuper',
	          defaultValue:"main",
	          description: "Branch/Tag do git para o SUPER")
	      string(
	          name: 'gitSuperAddress',
	          defaultValue:"git@github.com:supergovbr/super",
	          description: "Endereco git do Fonte do Super")
	      string(
	          name: 'gitSuperKey',
	          defaultValue:"CredGitSuper",
	          description: "Chave git em formato base64 em jenkins secret")
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
                    GITURL = "https://github.com/supergovbr/super-docker.git"
					          GITCRED = ""
					          GITSUPERVERSAO = params.versaoSuper
                    GITSUPERKEY = params.gitSuperKey

                    SERVICOPD_INSTALAR = params.servicoProtocoloDigitalInstalar
                    SERVICOPD_SIGLA = params.servicoProtocoloDigitalSigla
                    SERVICOPD_NOME = params.servicoProtocoloDigitalNome
                    SERVICOPD_OPERACOES = params.servicoProtocoloDigitalOperacoes

                    MODULORESPOSTA_INSTALAR = params.moduloRespostaInstalar
                    MODULORESPOSTA_VERSAO = params.moduloRespostaVersao
                    MODULORESPOSTA_SISTEMA_ID = params.moduloRespostaSistemaId
                    MODULORESPOSTA_DOCUMENTO_ID = params.moduloRespostaDocumentoId

                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        warning('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

                }

                sh """
                echo ${WORKSPACE}
                ls -lha

                cd super || true
                make  destroy || true

                cd ${WORKSPACE}
                sudo rm -rf super
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
                        //credentialsId: GITCRED,
                        url: GITURL

                    sh """

                    ls -l
                    """

                }
            }
        }

        stage('Deletar e Subir Projeto Kubernetes'){

            steps {
                dir('kube'){

                    withCredentials([ string(credentialsId: GITSUPERKEY, variable: 'LHAVE')]) {

                        sh """

                        cd infra
                        echo "" >> envlocal.env
                        echo "export APP_FONTES_GIT_PRIVKEY_BASE64=${LHAVE}" >> envlocal.env

                        """
                    }

                    sh """
                    cd infra
                    echo "" >> envlocal.env
                    echo "export APP_HOST=super.pd.teste.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=git@github.com:supergovbr/super" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${VERSAOSUPER}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=superns-pd" >> envlocal.env
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
                    make kubernetes_apply
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
                            make kube_timeout=300s KUBE_DEPLOY_NAME=sei-app kubernetes_check_deploy_generic
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
                            make check_isalive-timeout=60 check-super-isalive
                            """
                        }
                    }
                }



            }

        }


    }

}
