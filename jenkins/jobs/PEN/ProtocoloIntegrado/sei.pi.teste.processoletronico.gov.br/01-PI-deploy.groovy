/*

Job para Publicar o SEI em Cluster Kubernetes

*/

pipeline {
    agent {
        node{
            label "SUPER-FONTE"
        }
    }

    parameters {
      
          string(
	          name: 'versaoSei',
	          defaultValue:"4.0.9",
	          description: "Versao para o SEI (4.0.0 a 4.0.11)")
	      string(
	          name: 'gitSeiAddress',
	          defaultValue:"git@github.com:supergovbr/super",
	          description: "Endereco git do Fonte do SEI")
	      string(
	          name: 'gitSeiKey',
	          defaultValue:"CredGitSuper",
	          description: "Chave git em formato base64 em jenkins secret")
	      string(
	          name: 'moduloPIVersao',
	          defaultValue:"master",
	          description: "Versao do Módulo Protocolo Integrado")
	      string(
	          name: 'moduloPIUrl',
	          defaultValue:"https://protocolointegrado.preprod.nuvem.gov.br/ProtocoloWS/integradorService?wsdl",
	          description: "Url para Envio das Informações")
	      string(
	          name: 'moduloPIUsuario',
	          defaultValue:"credModuloPIUsuaro",
	          description: "Usuário do Protocolo Integrado, nao altere")
          string(
              name: 'moduloPISenha',
              defaultValue:"credModuloPISenha",
              description: "Senha do Protocolo Integrado, nao altere")
          
	      string(
	          name: 'moduloPIEmail',
	          defaultValue:"example@example.com",
	          description: "Email do Módulo do Protocolo Integrado")
              
    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{
                    GITURL = "https://github.com/spbgovbr/sei-docker.git"
					GITCRED = ""
					GITSEIVERSAO = params.versaoSei
                    GITSEIKEY = params.gitSeiKey
                    
                    MODULOPI_INSTALAR = "true"
                    MODULOPI_VERSAO = params.moduloPIVersao
                    MODULOPI_URL = params.moduloPIUrl
                    MODULOPI_USUARIO = params.moduloPIUsuario
                    MODULOPI_SENHA = params.moduloPISenha
                    MODULOPI_EMAIL = params.moduloPIemail

                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        warning('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

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
                    
                    withCredentials([ string(credentialsId: GITSEIKEY, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "" >> envlocal.env
                        echo "export APP_FONTES_GIT_PRIVKEY_BASE64=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    sh """
                    cd infra
                    echo "" >> envlocal.env
                    echo "export APP_HOST=sei.pi.teste.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=git@github.com:supergovbr/super" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${VERSAOSEI}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=sei-pi" >> envlocal.env
                    echo "export KUBERNETES_PVC_STORAGECLASS=nfs-client2" >> envlocal.env
                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env
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

                    echo "export MODULO_PI_INSTALAR=${MODULOPI_INSTALAR}" >> envlocal.env
                    echo "export MODULO_PI_VERSAO=${MODULOPI_VERSAO}" >> envlocal.env
                    echo "export MODULO_PI_URL=${MODULOPI_URL}" >> envlocal.env
                    echo "export MODULO_PI_EMAIL=${MODULOPI_EMAIL}" >> envlocal.env

		    echo "export PROTOCOLO_INTEGRADO_API_REST=${MODULOPI_URL}" >> envlocal.env
                        
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
                            echo "export APP_HOST=sei.pi.teste.processoeletronico.gov.br" >> envlocal.env
                            make check_isalive-timeout=120 check-sei-isalive
                            """
                        }
                    }
                }
                
                
                
            }
            
        }


    }
    
}

