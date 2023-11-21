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
	      string(
	          name: 'gitSeiAddress',
	          defaultValue:"git@github.com:supergovbr/super",
	          description: "Endereco git do Fonte do Super")
	      string(
	          name: 'gitSeiKey',
	          defaultValue:"CredGitSuper",
	          description: "Chave git em formato base64 em jenkins secret")
          choice(
            name: 'moduloPenInstalar', 
            choices: ['true', 'false'], 
            description: 'Instalar Módulo PEN')
	      string(
	          name: 'moduloPenVersao',
	          defaultValue:"master",
	          description: "Versao do Módulo PEN")
	      string(
	          name: 'moduloPenCert',
	          defaultValue:"credModuloPenCertOrgao6",
	          description: "Certificado base64 do módulo em jenkins secret")
	      string(
	          name: 'moduloPenCertSenha',
	          defaultValue:"credModuloPenCertSenhaOrgao6",
	          description: "Senha do Certificado do módulo em jenkins secret")
	      string(
	          name: 'moduloPenGearmanIp',
	          defaultValue:"127.0.0.1",
	          description: "Caso queira usar gearman informe o endereco. Caso n queira deixe em branco")
	      string(
	          name: 'moduloPenGearmanPorta',
	          defaultValue:"4730",
	          description: "Caso queira usar gearman informe a porta. Caso n queira deixe em branco")
	      string(
	          name: 'moduloPenRepositorioOrigem',
	          defaultValue:"37",
	          description: "Repositorio de Origem do Módulo")
	      string(
	          name: 'moduloPenTipoProcessoExterno',
	          defaultValue:"100000256",
	          description: "Tipo de Processo para o Módulo PEN")
	      string(
	          name: 'moduloPenUnidadeGeradora',
	          defaultValue:"110000003",
	          description: "Unidade do PEN")
	      string(
	          name: 'moduloPenUnidadeAssociacaoSuper',
	          defaultValue:"110000001",
	          description: "Unidade Associação do Super")
	      string(
	          name: 'moduloPenUnidadeAssociacaoPen',
	          defaultValue:"151861",
	          description: "Unidade Associação do PEN")

    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{
                    GITURL = "https://github.com/spbgovbr/sei-docker.git"
					GITCRED = ""
					GITSEIVERSAO = params.versaoSei
                    GITSEIKEY = params.gitSeiKey
                    
                    MODULOPEN_INSTALAR = params.moduloPenInstalar
                    MODULOPEN_VERSAO = params.moduloPenVersao
                    MODULOPEN_CERT = params.moduloPenCert
                    MODULOPEN_CERTSENHA = params.moduloPenCertSenha
                    MODULOPEN_GEARMAN_IP = params.moduloPenGearmanIp
                    MODULOPEN_GEARMAN_PORTA = params.moduloPenGearmanPorta
                    MODULOPEN_REPOSITORIOORIGEM = params.moduloPenRepositorioOrigem
                    MODULOPEN_TIPOPROCESSO = params.moduloPenTipoProcessoExterno
                    MODULOPEN_UNIDADEGERADORA = params.moduloPenUnidadeGeradora
                    MODULOPEN_UNIDADEASSOCIACAOSUPER = params.moduloPenUnidadeAssociacaoSuper
                    MODULOPEN_UNIDADEASSOCIACAOPEN = params.moduloPenUnidadeAssociacaoPen

                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        warning('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

                }

                sh """
                echo ${WORKSPACE}
                ls -lha
				
                sudo rm -rf kube
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
                    
                    sh """
                    cd infra
                    echo "" >> envlocal.env
                    echo "export APP_HOST=super.orgao6.tramita.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_ORGAO=ORGAO6" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=git@github.com:supergovbr/super" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${GITSEIVERSAO}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=mod-sei-pen-orgao6" >> envlocal.env
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

                    echo "export MODULO_PEN_INSTALAR=${MODULOPEN_INSTALAR}" >> envlocal.env
                    echo "export MODULO_PEN_VERSAO=${MODULOPEN_VERSAO}" >> envlocal.env
                    """
                    
                    withCredentials([ string(credentialsId: MODULOPEN_CERT, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "export MODULO_PEN_CERTIFICADO_BASE64=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    withCredentials([ string(credentialsId: MODULOPEN_CERTSENHA, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "export MODULO_PEN_CERTIFICADO_SENHA=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    sh """
                    
                    cd infra
                    echo "export MODULO_PEN_GEARMAN_IP=${MODULOPEN_GEARMAN_IP}" >> envlocal.env
                    echo "export MODULO_PEN_GEARMAN_PORTA=${MODULOPEN_GEARMAN_PORTA}" >> envlocal.env
                    echo "export MODULO_PEN_REPOSITORIO_ORIGEM=${MODULOPEN_REPOSITORIOORIGEM}" >> envlocal.env
                    echo "export MODULO_PEN_TIPO_PROCESSO_EXTERNO=${MODULOPEN_TIPOPROCESSO}" >> envlocal.env
                    echo "export MODULO_PEN_UNIDADE_GERADORA=${MODULOPEN_UNIDADEGERADORA}" >> envlocal.env
                    echo "export MODULO_PEN_UNIDADE_ASSOCIACAO_PEN=${MODULOPEN_UNIDADEASSOCIACAOPEN}" >> envlocal.env
                    echo "export MODULO_PEN_UNIDADE_ASSOCIACAO_SUPER=${MODULOPEN_UNIDADEASSOCIACAOSUPER}" >> envlocal.env

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
                            make kube_timeout=600s KUBE_DEPLOY_NAME=sei-app kubernetes_check_deploy_generic
                            """
                        }
                    }
                }
                
                
                stage('URL Respondendo'){
                    steps {
                        dir('kube'){
                            sh """
                            cd infra
                            echo "export APP_HOST=super.orgao6.tramita.processoeletronico.gov.br" >> envlocal.env
                            make check_isalive-timeout=600 check-sei-isalive
                            """
                        }
                    }
                }
                
                
                
            }
            
        }


    }
    
}
