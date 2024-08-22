/*
0101
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
	          name: 'moduloPenUnidadeAssociacaoSei',
	          defaultValue:"110000001",
	          description: "Unidade Associação do Sei")
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
            name: 'moduloWSInstalar', 
            choices: ['true', 'false'], 
            description: 'Instalar Módulo WS')
	      string(
	          name: 'moduloWSVersao',
	          defaultValue:"master",
	          description: "Versao do Módulo WS")
	      string(
	          name: 'moduloWSUrlNotificacao',
	          defaultValue:"https://app-push-gestao-api.dev.nuvem.gov.br/mba-mmmessage/message",
	          description: "Url do serviço de Notificação do WS")
	      string(
	          name: 'moduloWSSIdApp',
	          defaultValue:"4",
	          description: "Id App do WS")
	      string(
	          name: 'moduloWSChave',
	          defaultValue:"credModWsSuperChave",
	          description: "Chave para o serviço de notificação em jenkins secret")
	      string(
	          name: 'moduloWSToken',
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
	          defaultValue:"credLoginUnicoSecretValidacao",
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
                    GITURL = "https://github.com/spbgovbr/sei-docker.git"
					          GITCRED = ""
					          GITSEIVERSAO = params.versaoSei
                    GITSEIKEY = params.gitSeiKey
                    
                    GITSEIADDRESS = params.gitSeiAddress
                    
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
                    MODULOPEN_UNIDADEASSOCIACAOSEI = params.moduloPenUnidadeAssociacaoSei
                    MODULOPEN_UNIDADEASSOCIACAOPEN = params.moduloPenUnidadeAssociacaoPen
                    
                    MODULOGD_INSTALAR = params.moduloGDInstalar
                    MODULOGD_VERSAO = params.moduloGDVersao
                    
                    MODULOWS_INSTALAR = params.moduloWSInstalar
                    MODULOWS_VERSAO = params.moduloWSVersao
                    MODULOWS_URLNOTIFICACAO = params.moduloWSUrlNotificacao
                    MODULOWS_IDAPP = params.moduloWSIdApp
                    MODULOWS_CHAVE = params.moduloWSChave
                    MODULOWS_TOKEN = params.moduloWSToken
                    
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
                    echo "export APP_HOST=super.dev.processoeletronico.gov.br" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=${GITSEIADDRESS}" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${VERSAOSEI}" >> envlocal.env
                    echo "export APP_MAIL_SERVIDOR=relay.nuvem.gov.br" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=superns" >> envlocal.env
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
                    echo "export MODULO_PEN_UNIDADE_ASSOCIACAO_SEI=${MODULOPEN_UNIDADEASSOCIACAOSEI}" >> envlocal.env
                    
                    echo "export MODULO_GESTAODOCUMENTAL_INSTALAR=${MODULOGD_INSTALAR}" >> envlocal.env
                    echo "export MODULO_GESTAODOCUMENTAL_VERSAO=${MODULOGD_VERSAO}" >> envlocal.env
                    
                    echo "export MODULO_WS_INSTALAR=${MODULOWS_INSTALAR}" >> envlocal.env
                    echo "export MODULO_WS_VERSAO=${MODULOWS_VERSAO}" >> envlocal.env
                    echo "export MODULO_WS_URL_NOTIFICACAO=${MODULOWS_URLNOTIFICACAO}" >> envlocal.env
                    echo "export MODULO_WS_ID_APP=${MODULOWS_IDAPP}" >> envlocal.env
                    echo "export MODULO_WS_TOKEN_SECRET=${MODULOWS_TOKEN}" >> envlocal.env
                    
                    """
                    
                    withCredentials([ string(credentialsId: MODULOWS_CHAVE, variable: 'LHAVE')]) {
                            
                        sh """
                      
                        cd infra
                        echo "export MODULO_WS_CHAVE=${LHAVE}" >> envlocal.env
                      
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
                            echo "export APP_HOST=super.dev.processoeletronico.gov.br" >> envlocal.env
                            make check_isalive-timeout=900 check-sei-isalive
                            """
                        }
                    }
                }
                
                
                
            }
            
        }


    }
    
}
