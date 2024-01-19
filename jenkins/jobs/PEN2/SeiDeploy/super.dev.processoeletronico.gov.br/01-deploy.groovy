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
              choices: ['false', 'true'], 
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
            name: 'moduloEstatisticaInstalar', 
            choices: ['true', 'false'], 
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
	          defaultValue:"credModuloEstatisticaChave",
	          description: "Chave para envio dos dados em jenkins secret")

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
	          defaultValue:"credModuloPenCert",
	          description: "Certificado base64 do módulo em jenkins secret")
	      string(
	          name: 'moduloPenCertSenha',
	          defaultValue:"credModuloPenCertSenha",
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
	          defaultValue:"125025",
	          description: "Unidade Associação do PEN")

        choice(
            name: 'moduloGDInstalar', 
            choices: ['true', 'false'], 
            description: 'Instalar Módulo Gestão Documental')
	      string(
	          name: 'moduloGDVersao',
	          defaultValue:"master",
	          description: "Versão do Gestão Documental")

        choice(
            name: 'moduloWSSuperInstalar', 
            choices: ['true', 'false'], 
            description: 'Instalar Módulo WSSUPER - WSSEI')
	      string(
	          name: 'moduloWSSuperVersao',
	          defaultValue:"master",
	          description: "Versao do Módulo WSSUPER")
	      string(
	          name: 'moduloWSSuperUrlNotificacao',
	          defaultValue:"https://app-push-gestao-api.dev.nuvem.gov.br/mba-mmmessage/message",
	          description: "Url do serviço de Notificação do WSSUPER")
	      string(
	          name: 'moduloWSSuperIdApp',
	          defaultValue:"4",
	          description: "Id App do WSSUPER")
	      string(
	          name: 'moduloWSSuperChave',
	          defaultValue:"credModWsSuperChave",
	          description: "Chave para o serviço de notificação em jenkins secret")
	      string(
	          name: 'moduloWSSuperToken',
	          defaultValue:"504CE1E9-8913-488F-AB3E-EDDABC065B0B",
	          description: "Token para o serviço de notificação")
            
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

        choice(
            name: 'moduloLoginUnicoInstalar', 
            choices: ['true', 'false'], 
            description: 'Instalar Módulo Login Unico')
	      string(
	          name: 'moduloLoginUnicoVersao',
	          defaultValue:"master",
	          description: "Versao do Módulo Login Unico")
	      string(
	          name: 'moduloLoginUnicoClientId',
	          defaultValue:"sistemas/homologacao/sei/controlador_externo",
	          description: "Client Id do Login Unico")
	      string(
	          name: 'moduloLoginUnicoSecret',
	          defaultValue:"AKymcJm_lNOzfo5p-FflKC6uYLgTTHWfAbFpQFmb64I4kNvT4yEoAvgIlDtR17-FlTI4BcUKXH6E9OejIGPbLQA",
	          description: "Segredo do Login Unico")
	      string(
	          name: 'moduloLoginUnicoUrlProvider',
	          defaultValue:"https://sso.staging.acesso.gov.br/",
	          description: "Url Provider do Login Unico")
	      string(
	          name: 'moduloLoginUnicoRedirectUrl',
	          defaultValue:"https://super.dev.processoeletronico.gov.br/sei/modulos/loginunico/controlador_loginunico.php",
	          description: "Url de redirect do Login Unico")        
	      string(
	          name: 'moduloLoginUnicoUrlLogout',
	          defaultValue:"https://super.dev.processoeletronico.gov.br/sei/modulos/loginunico/logout.php",
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
	          defaultValue:"AKymcJm_lNOzfo5p-FflKC6uYLgTTHWfAbFpQFmb64I4kNvT4yEoAvgIlDtR17-FlTI4BcUKXH6E9OejIGPbLQB",
	          description: "Secret de Validação Login Unico")
	      string(
	          name: 'moduloLoginUnicoOrgao',
	          defaultValue:"0",
	          description: "Orgão para aceitação do Login Unico")

        choice(
            name: 'moduloAssinaturaInstalar', 
            choices: ['true', 'false'], 
            description: 'Instalar Módulo Assinatura Avançada')
	      string(
	          name: 'moduloAssinaturaVersao',
	          defaultValue:"master",
	          description: "Versão do Módulo Assinatura Avançada")
	      string(
	          name: 'moduloAssinaturaClientID',
	          defaultValue:"assinaturaAvancadaSeges",
	          description: "Client Id do Assinatura Avançada")
	      string(
	          name: 'moduloAssinaturaSecret',
	          defaultValue:"dIEeKdctWWQQHxXFuutj",
	          description: "Segredo da Assinatura Avançada")
	      string(
	          name: 'moduloAssinaturaUrlProvider',
	          defaultValue:"https://cas.staging.iti.br/oauth2.0",
	          description: "Url Provider da Assinatura Avançada")
	      string(
	          name: 'moduloAssinaturaUrlServicos',
	          defaultValue:"assinatura-api.staging.iti.br/externo/v2",
	          description: "Url de Serviços Assinatura Avancada")
	          
	          
	      choice(
            name: 'moduloPeticionamentoInstalar', 
            choices: ['true', 'false'], 
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
                    GITURL = "https://github.com/supergovbr/super-docker.git"
					          GITCRED = ""
					          GITSUPERVERSAO = params.versaoSuper
                    GITSUPERKEY = params.gitSuperKey
                    
                    SERVICOPD_INSTALAR = params.servicoProtocoloDigitalInstalar
                    SERVICOPD_SIGLA = params.servicoProtocoloDigitalSigla
                    SERVICOPD_NOME = params.servicoProtocoloDigitalNome
                    SERVICOPD_OPERACOES = params.servicoProtocoloDigitalOperacoes
                    
                    MODULOESTATISTICA_INSTALAR = params.moduloEstatisticaInstalar
                    MODULOESTATISTICA_VERSAO = params.moduloEstatisticaVersao
                    MODULOESTATISTICA_URL = params.moduloEstatisticasUrl
                    MODULOESTATISTICA_SIGLA = params.moduloEstatisticaSigla
                    MODULOESTATISTICA_CHAVE = params.moduloEstatisticaChave
                    
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
                    
                    MODULOGD_INSTALAR = params.moduloGDInstalar
                    MODULOGD_VERSAO = params.moduloGDVersao
                    
                    MODULOWSSUPER_INSTALAR = params.moduloWSSuperInstalar
                    MODULOWSSUPER_VERSAO = params.moduloWSSuperVersao
                    MODULOWSSUPER_URLNOTIFICACAO = params.moduloWSSuperUrlNotificacao
                    MODULOWSSUPER_IDAPP = params.moduloWSSuperIdApp
                    MODULOWSSUPER_CHAVE = params.moduloWSSuperChave
                    MODULOWSSUPER_TOKEN = params.moduloWSSuperToken
                    
                    MODULORESPOSTA_INSTALAR = params.moduloRespostaInstalar
                    MODULORESPOSTA_VERSAO = params.moduloRespostaVersao
                    MODULORESPOSTA_SISTEMA_ID = params.moduloRespostaSistemaId
                    MODULORESPOSTA_DOCUMENTO_ID = params.moduloRespostaDocumentoId
                    
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
                    echo "export APP_HOST=super.dev.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=git@github.com:supergovbr/super" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${VERSAOSUPER}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=superns" >> envlocal.env
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
                    
                    echo "export MODULO_ESTATISTICAS_INSTALAR=${MODULOESTATISTICA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_VERSAO=${MODULOESTATISTICA_VERSAO}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_SIGLA=${MODULOESTATISTICA_SIGLA}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_URL=${MODULOESTATISTICA_URL}" >> envlocal.env
                    
                    """
                    
                    withCredentials([ string(credentialsId: MODULOESTATISTICA_CHAVE, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "export MODULO_ESTATISTICAS_CHAVE=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    sh """
                    
                    cd infra
                    
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
                    
                    echo "export MODULO_GESTAODOCUMENTAL_INSTALAR=${MODULOGD_INSTALAR}" >> envlocal.env
                    echo "export MODULO_GESTAODOCUMENTAL_VERSAO=${MODULOGD_VERSAO}" >> envlocal.env
                    
                    echo "export MODULO_WSSUPER_INSTALAR=${MODULOWSSUPER_INSTALAR}" >> envlocal.env
                    echo "export MODULO_WSSUPER_VERSAO=${MODULOWSSUPER_VERSAO}" >> envlocal.env
                    echo "export MODULO_WSSUPER_URL_NOTIFICACAO=${MODULOWSSUPER_URLNOTIFICACAO}" >> envlocal.env
                    echo "export MODULO_WSSUPER_ID_APP=${MODULOWSSUPER_IDAPP}" >> envlocal.env
                    echo "export MODULO_WSSUPER_TOKEN_SECRET=${MODULOWSSUPER_TOKEN}" >> envlocal.env
                    
                    """
                    
                    withCredentials([ string(credentialsId: MODULOWSSUPER_CHAVE, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "export MODULO_WSSUPER_CHAVE=${LHAVE}" >> envlocal.env
                      
                        """
                    }
                    
                    
                    sh """
                    cd infra
                    
                    
                    echo "export MODULO_RESPOSTA_INSTALAR=${MODULORESPOSTA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_RESPOSTA_VERSAO=${MODULORESPOSTA_VERSAO}" >> envlocal.env
                    echo "export MODULO_RESPOSTA_SISTEMA_ID=${MODULORESPOSTA_SISTEMA_ID}" >> envlocal.env
                    echo "export MODULO_RESPOSTA_DOCUMENTO_ID=${MODULORESPOSTA_DOCUMENTO_ID}" >> envlocal.env
                    
                    echo "export MODULO_LOGINUNICO_INSTALAR=${MODULOLOGINUNICO_INSTALAR}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_VERSAO=${MODULOLOGINUNICO_VERSAO}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_CLIENTID=${MODULOLOGINUNICO_CLIENTID}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_SECRET=${MODULOLOGINUNICO_SECRET}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_URLPROVIDER=${MODULOLOGINUNICO_URLPROVIDER}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_REDIRECTURL=${MODULOLOGINUNICO_REDIRECTURL}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_URLLOGOUT=${MODULOLOGINUNICO_URLLOGOUT}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_SCOPE=${MODULOLOGINUNICO_SCOPE}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_URLSERVICOS=${MODULOLOGINUNICO_SERVICOS}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_URLREVALIDACAO=${MODULOLOGINUNICO_REVALIDACAO}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_CIENTIDVALIDACAO=${MODULOLOGINUNICO_CLIENTIDVALIDACAO}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_SECRETVALIDACAO=${MODULOLOGINUNICO_SECRETVALIDACAO}" >> envlocal.env
                    echo "export MODULO_LOGINUNICO_ORGAO=${MODULOLOGINUNICO_ORGAO}" >> envlocal.env
                    
                    echo "export MODULO_ASSINATURAVANCADA_INSTALAR=${MODULOASSINATURA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_VERSAO=${MODULOASSINATURA_VERSAO}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_CLIENTID=${MODULOASSINATURA_CLIENTID}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_SECRET=${MODULOASSINATURA_SECRET}" >> envlocal.env
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
                            echo "export APP_HOST=super.dev.processoeletronico.gov.br" >> envlocal.env
                            make check_isalive-timeout=60 check-super-isalive
                            """
                        }
                    }
                }
                
                
                
            }
            
        }


    }
    
}
