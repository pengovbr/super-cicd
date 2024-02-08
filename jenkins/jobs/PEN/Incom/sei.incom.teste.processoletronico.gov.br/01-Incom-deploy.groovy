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
	          name: 'moduloIncomVersao',
	          defaultValue:"v1.0.4",
	          description: "Versao do Módulo Incom")
	      string(
	          name: 'moduloIncomUrl',
	          defaultValue:"https://seiwsincom2.in.gov.br/seiwsincom/services/servicoIN?wsdl",
	          description: "Url para Envio das Informações")
	      string(
	          name: 'moduloIncomUsuario',
	          defaultValue:"credModuloIncomUsuario",
	          description: "Usuário do Incom, nao altere")
          string(
              name: 'moduloIncomSenha',
              defaultValue:"credModuloIncomSenha",
              description: "Senha do Incom, nao altere")
          
	      string(
	          name: 'moduloIncomSiorg',
	          defaultValue:"308803",
	          description: "Siorg do Orgao Incom")
              
    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{
                    GITURL = "https://github.com/spbgovbr/sei-docker.git"
					GITCRED = ""
					GITSEIVERSAO = params.versaoSei
                    GITSEIKEY = params.gitSeiKey
                    
                    MODULOINCOM_INSTALAR = "true"
                    MODULOINCOM_VERSAO = params.moduloIncomVersao
                    MODULOINCOM_URL = params.moduloIncomUrl
                    MODULOINCOM_USUARIO = params.moduloIncomUsuario
                    MODULOINCOM_SENHA = params.moduloIncomSenha
                    MODULOINCOM_SIORG = params.moduloIncomSiorg

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

                    git branch: 'implement-incom',
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
                    echo "export APP_HOST=sei.incom.teste.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=git@github.com:supergovbr/super" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${VERSAOSEI}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=sei-incom" >> envlocal.env
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

                    echo "export MODULO_INCOM_INSTALAR=${MODULOINCOM_INSTALAR}" >> envlocal.env
                    echo "export MODULO_INCOM_VERSAO=${MODULOINCOM_VERSAO}" >> envlocal.env
                    echo "export MODULO_INCOM_URL=${MODULOINCOM_URL}" >> envlocal.env
                    echo "export MODULO_INCOM_SIORG=${MODULOINCOM_SIORG}" >> envlocal.env
                    
                    """
                    
                    withCredentials([ string(credentialsId: MODULOINCOM_USUARIO, variable: 'LHAVE')]) {
                        
                        sh """
                      
                        cd infra
                        echo "export MODULO_INCOM_USERWS=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    withCredentials([ string(credentialsId: MODULOINCOM_SENHA, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "export MODULO_INCOM_PASSWS=${LHAVE}" >> envlocal.env
                      
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
                            echo "export APP_HOST=sei.incom.teste.processoeletronico.gov.br" >> envlocal.env
                            make check_isalive-timeout=120 check-sei-isalive
                            """
                        }
                    }
                }
                
                
                
            }
            
        }


    }
    
}