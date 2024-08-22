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
            description: 'Atenção. A versão dos módulos ou do SEI pode ser o hash do commit; tag; branch;') 
        
	      string(
	          name: 'versaoSei',
	          defaultValue:"main",
	          description: "Branch/Tag do git para o SEI")
	      string(
	          name: 'gitSeiAddress',
	          defaultValue:"git@github.com:pengovbr/sei",
	          description: "Endereco git do Fonte do SEI")
	      string(
	          name: 'gitSeiKey',
	          defaultValue:"CredGitSuper",
	          description: "Chave git em formato base64 em jenkins secret")
        

        choice(
            name: 'moduloLoginUnicoInstalar', 
            choices: ['false', 'true'], 
            description: 'Instalar Módulo Login Unico')
	      string(
	          name: 'moduloLoginUnicoVersao',
	          defaultValue:"master",
	          description: "Versao do Módulo Login Unico")
	      string(
	          name: 'moduloLoginUnicoClientId',
	          defaultValue:"credLoginUnicoClientId",
	          description: "Client Id do Login Unico")
	      string(
	          name: 'moduloLoginUnicoSecret',
	          defaultValue:"credLoginUnicoSecret",
	          description: "Segredo do Login Unico")
	      string(
	          name: 'moduloLoginUnicoUrlProvider',
	          defaultValue:"https://sso.staging.acesso.gov.br/",
	          description: "Url Provider do Login Unico")
	      string(
	          name: 'moduloLoginUnicoRedirectUrl',
	          defaultValue:"https://sei.assinatura.teste.processoeletronico.gov.br/sei/modulos/loginunico/controlador_loginunico.php",
	          description: "Url de redirect do Login Unico")        
	      string(
	          name: 'moduloLoginUnicoUrlLogout',
	          defaultValue:"https://sei.assinatura.teste.processoeletronico.gov.br/sei/modulos/loginunico/logout.php",
	          description: "Url de Logout do Login Unico")          
	      string(
	          name: 'moduloLoginUnicoScope',
	          defaultValue:"openid+email+phone+profile+govbr_empresa+govbr_confiabilidades",
	          description: "Escopo do Login Unico")
	      string(
	          name: 'moduloLoginUnicoServicos',
	          defaultValue:"https://api.staging.acesso.gov.br/",
	          description: "Url de Serviços do Login Unico")
	      string(
	          name: 'moduloLoginUnicoRevalidacao',
	          defaultValue:"https://oauth.staging.acesso.gov.br/v1/",
	          description: "Url para Revalidação do Login Unico")
	      string(
	          name: 'moduloLoginUnicoClientIdValidacao',
	          defaultValue:"sei.resposta.nuvem.gov.br/validacaosenha",
	          description: "ClientId de Validação Login Unico")
	      string(
	          name: 'moduloLoginUnicoSecretValidacao',
	          defaultValue:"credLoginUnicoSecretValidacao",
	          description: "Secret de Validação Login Unico")
	      string(
	          name: 'moduloLoginUnicoOrgao',
	          defaultValue:"0",
	          description: "Orgão para aceitação do Login Unico")

        choice(
            name: 'moduloAssinaturaInstalar', 
            choices: ['false', 'true'], 
            description: 'Instalar Módulo Assinatura Avançada')
	      string(
	          name: 'moduloAssinaturaVersao',
	          defaultValue:"master",
	          description: "Versão do Módulo Assinatura Avançada")
	      string(
	          name: 'moduloAssinaturaClientID',
	          defaultValue:"credAssinaturaClientID",
	          description: "Client Id do Assinatura Avançada")
	      string(
	          name: 'moduloAssinaturaSecret',
	          defaultValue:"credAssinaturaSecret",
	          description: "Segredo da Assinatura Avançada")
	      string(
	          name: 'moduloAssinaturaUrlProvider',
	          defaultValue:"https://cas.staging.iti.br/oauth2.0",
	          description: "Url Provider da Assinatura Avançada")
	      string(
	          name: 'moduloAssinaturaUrlServicos',
	          defaultValue:"https://assinatura-api.staging.iti.br/externo/v2",
	          description: "Url de Serviços Assinatura Avancada")
	          
	          
	      choice(
            name: 'moduloPeticionamentoInstalar', 
            choices: ['false', 'true'], 
            description: 'Instalar Módulo Peticionamento')
	      string(
	          name: 'moduloPeticionamentoVersao',
	          defaultValue:"master",
	          description: "Versão do Módulo Peticionamento")
	      string(
	          name: 'moduloPeticionamentoURL',
	          defaultValue:"https://github.com/anatelgovbr/mod-sei-peticionamento",
	          description: "URL do modulo de peticionamento")

    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{
                    GITURL = "https://github.com/spbgovbr/sei-docker.git"
					          GITCRED = ""
					          GITSEIVERSAO = params.versaoSei
                    GITSEIKEY = params.gitSeiKey
                    
                    GITSEIADDRESS = params.gitSeiAddress
                    
                    MODULOLOGINUNICO_INSTALAR = params.moduloLoginUnicoInstalar
                    MODULOLOGINUNICO_VERSAO = params.moduloLoginUnicoVersao
                    MODULOLOGINUNICO_CLIENTID = params.moduloLoginUnicoClientId
                    MODULOLOGINUNICO_SECRET = params.moduloLoginUnicoSecret
                    MODULOLOGINUNICO_URLPROVIDER = params.moduloLoginUnicoUrlProvider
                    MODULOLOGINUNICO_REDIRECTURL = params.moduloLoginUnicoRedirectUrl
                    MODULOLOGINUNICO_URLLOGOUT = params.moduloLoginUnicoUrlLogout
                    MODULOLOGINUNICO_SCOPE = params.moduloLoginUnicoScope
                    MODULOLOGINUNICO_SERVICOS = params.moduloLoginUnicoServicos
                    MODULOLOGINUNICO_REVALIDACAO = params.moduloLoginUnicoRevalidacao
                    MODULOLOGINUNICO_CLIENTIDVALIDACAO = params.moduloLoginUnicoClientIdValidacao
                    MODULOLOGINUNICO_SECRETVALIDACAO = params.moduloLoginUnicoSecretValidacao
                    MODULOLOGINUNICO_ORGAO = params.moduloLoginUnicoOrgao
                    
                    MODULOASSINATURA_INSTALAR = params.moduloAssinaturaInstalar
                    MODULOASSINATURA_VERSAO = params.moduloAssinaturaVersao
                    MODULOASSINATURA_CLIENTID = params.moduloAssinaturaClientID
                    MODULOASSINATURA_SECRET = params.moduloAssinaturaSecret
                    MODULOASSINATURA_URLPROVIDER = params.moduloAssinaturaUrlProvider
                    MODULOASSINATURA_URLSERVICOS = params.moduloAssinaturaUrlServicos
                    
                    MODULOPETICIONAMENTO_INSTALAR = params.moduloPeticionamentoInstalar
                    MODULOPETICIONAMENTO_VERSAO = params.moduloPeticionamentoVersao
                    MODULOPETICIONAMENTO_URL = params.moduloPeticionamentoURL
                    
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
                    
                    withCredentials([ string(credentialsId: MODULOLOGINUNICO_CLIENTID, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "" >> envlocal.env
                        echo "export MODULO_LOGINUNICO_CLIENTID=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    withCredentials([ string(credentialsId: MODULOLOGINUNICO_SECRET, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "" >> envlocal.env
                        echo "export MODULO_LOGINUNICO_SECRET=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    withCredentials([ string(credentialsId: MODULOLOGINUNICO_SECRETVALIDACAO, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "" >> envlocal.env
                        echo "export MODULO_LOGINUNICO_SECRETVALIDACAO=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    withCredentials([ string(credentialsId: MODULOASSINATURA_CLIENTID, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "" >> envlocal.env
                        echo "export MODULO_ASSINATURAVANCADA_CLIENTID=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    withCredentials([ string(credentialsId: MODULOASSINATURA_SECRET, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "" >> envlocal.env
                        echo "export MODULO_ASSINATURAVANCADA_SECRET=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                                        
                    sh """
                    cd infra
                    echo "" >> envlocal.env
                    echo "export APP_HOST=sei.assinatura.teste.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=${GITSEIADDRESS}" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${VERSAOSEI}" >> envlocal.env
                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env
                    echo "export APP_MAIL_SERVIDOR=relay.nuvem.gov.br" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=sei-assinatura" >> envlocal.env
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
                    
                    echo "export MODULO_ASSINATURAVANCADA_INSTALAR=${MODULOASSINATURA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_VERSAO=${MODULOASSINATURA_VERSAO}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_URLPROVIDER=${MODULOASSINATURA_URLPROVIDER}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_URL_SERVICOS=${MODULOASSINATURA_URLSERVICOS}" >> envlocal.env
                    
                    echo "export MODULO_PETICIONAMENTO_INSTALAR=${MODULOPETICIONAMENTO_INSTALAR}" >> envlocal.env
                    echo "export MODULO_PETICIONAMENTO_VERSAO=${MODULOPETICIONAMENTO_VERSAO}" >> envlocal.env
                    echo "export MODULO_PETICIONAMENTO_URL=${MODULOPETICIONAMENTO_URL}" >> envlocal.env
                    
                    
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
                            echo "export APP_HOST=sei.assinatura.teste.processoeletronico.gov.br" >> envlocal.env
                            make check_isalive-timeout=900 check-sei-isalive
                            """
                        }
                    }
                }
                
                
                
            }
            
        }


    }
    
}
