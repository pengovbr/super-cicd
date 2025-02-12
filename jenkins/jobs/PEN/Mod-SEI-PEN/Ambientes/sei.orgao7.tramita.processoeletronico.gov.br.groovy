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
            defaultValue:"git@github.com:pengovbr/sei",
            description: "Endereco git do Fonte do Sei")
        string(
            name: 'gitSeiKeyJenkins',
            defaultValue:"gitcredsuper",
            description: "Secret Jenkins para o Repo")
        string(
            name: 'gitSeiKey',
            defaultValue:"CredGitSuper",
            description: "Chave git em formato base64 em jenkins secret")
        choice(
            name: 'multiorgao',
            choices: ['false', 'true'],
            description: 'Caso deseje criar um ambiente Multiorgao marque true e preencha corretamente os dois campos abaixo')
        string(
            name: 'multiorgaoSiglas',
            defaultValue:"SIGLA1/SIGLA2",
            description: "Depois de habilitar Multiorgao acima. Passe aqui as siglas desejadas separadas por /")
        string(
            name: 'multiorgaoNomes',
            defaultValue:"nome do orgao1/nome do orgao2",
            description: "Depois de habilitar Multiorgao acima. Passe aqui os nomes respectivos às siglas separadas por /. Informar a mesma qtde de siglas e nomes, caso contrário o parametro será ignorado na instalação")
        choice(
            name: 'federacao',
            choices: ['false', 'true'],
            description: 'Caso deseje habilitar a federação nessa instalação, informe true. Após a criação do ambiente você terá que configurar com que outro ambiente será a federação')
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
            defaultValue:"pass",
            description: "Chave para envio dos dados")
        choice(
            name: 'moduloPenInstalar',
            choices: ['true', 'false'],
            description: 'Instalar Módulo PEN')
        string(
            name: 'moduloPenVersao',
            defaultValue:"master",
            description: "Versao do Módulo PEN")
        choice(
            name: 'moduloPenEndpoint',
            choices: ['soap', 'rest'],
            description: 'Antes de subir o ambiente, verifique se o módulo é compatível com SOAP ou REST e ajuste aqui')
        choice(
            name: 'moduloPenConfigurar',
            choices: ['true', 'false'],
            description: 'Caso deseje que o módulo confgure automaticamente para envio e recebimento. Se marcar falso, deverá configurar no menu de admin do módulo')
        string(
            name: 'moduloPenCert',
            defaultValue:"credModuloPenCertOrgao7",
            description: "Certificado base64 do módulo em jenkins secret")
        string(
            name: 'moduloPenCertSenha',
            defaultValue:"credModuloPenCertSenhaOrgao7",
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
            description: "Unidade Associação do Super")
        string(
            name: 'moduloPenUnidadeAssociacaoPen',
            defaultValue:"158775",
            description: "Unidade Associação do PEN")
        choice(
            name: 'moduloPIInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Protocolo Integrado')
        string(
            name: 'moduloPIVersao',
            defaultValue:"master",
            description: "Versao do Módulo Protocolo Integrado. Vai apontar para o PI de homolog. Nome Orgao: Teste 5.0")
        string(
            name: 'moduloPIEmail',
            defaultValue:"example@example.com",
            description: "Email do Módulo do Protocolo Integrado")
        choice(
            name: 'moduloRestInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Rest')
        string(
            name: 'moduloRestVersao',
            defaultValue:"master",
            description: "Versao do Módulo Rest")

        choice(
            name: 'moduloLoginUnicoInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Login Unico')

        string(
            name: 'moduloLoginUnicoVersao',
            defaultValue:"master",
            description: "Versao do Módulo Login Unico")

        choice(
            name: 'moduloAssinaturaInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Assinatura Avançada')
        string(
            name: 'moduloAssinaturaVersao',
            defaultValue:"master",
            description: "Versão do Módulo Assinatura Avançada")


    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{

                    JOB_URL = "sei.orgao7.tramita.processoeletronico.gov.br"
                    JOB_ORGAO = "ORGAO7"
                    JOB_NS = "mod-sei-pen-orgao7"

                    JOB_MODULOPI_URL = "https://protocolointegrado.preprod.nuvem.gov.br/ProtocoloWS/integradorService?wsdl"
                    JOB_MODULOPI_USUARIO = "credModuloPIOrgao7Usuario"
                    JOB_MODULOPI_SENHA = "credModuloPIOrgao7Senha"

                    JOB_MODULOREST_URLNOTIFICACAO = "https://app-push-gestao-api.dev.nuvem.gov.br/mba-mmmessage/message"
                    JOB_MODULOREST_IDAPP = "4"
                    JOB_MODULOREST_CHAVE = "credModWsSuperChave"
                    JOB_MODULOREST_TOKEN = "504CE1E9-8913-488F-AB3E-EDDABC065B07"

                    JOB_MODULOLOGINUNICO_CLIENTID = "credLoginUnicoClientId"
                    JOB_MODULOLOGINUNICO_SECRET = "credLoginUnicoSecret"
                    JOB_MODULOLOGINUNICO_URLPROVIDER = "https://sso.staging.acesso.gov.br/"
                    JOB_MODULOLOGINUNICO_REDIRECTURL = "https://sei.orgao7.tramita.processoeletronico.gov.br/sei/modulos/loginunico/controlador_loginunico.php"
                    JOB_MODULOLOGINUNICO_URLLOGOUT = "https://sei.orgao7.tramita.processoeletronico.gov.br/sei/modulos/loginunico/logout.php"
                    JOB_MODULOLOGINUNICO_SCOPE = "openid+email+phone+profile+govbr_empresa+govbr_confiabilidades"
                    JOB_MODULOLOGINUNICO_SERVICOS = "https://api.staging.acesso.gov.br/"
                    JOB_MODULOLOGINUNICO_REVALIDACAO = "https://oauth.staging.acesso.gov.br/v1/"
                    JOB_MODULOLOGINUNICO_CLIENTIDVALIDACAO = "sei.resposta.nuvem.gov.br/validacaosenha"
                    JOB_MODULOLOGINUNICO_SECRETVALIDACAO = "credLoginUnicoSecretValidacao"
                    JOB_MODULOLOGINUNICO_ORGAO = "0"

                    JOB_MODULOASSINATURA_CLIENTID = "credAssinaturaClientID"
                    JOB_MODULOASSINATURA_SECRET = "credAssinaturaSecret"
                    JOB_MODULOASSINATURA_URLPROVIDER = "https://cas.staging.iti.br/oauth2.0"
                    JOB_MODULOASSINATURA_URLSERVICOS = "https://assinatura-api.staging.iti.br/externo/v2"

                    JOB_MODULO_ASSINATURAVANCADA_VALIDAR_API_URL="https://h-api.iti.gov.br/validar"
                    JOB_MODULO_ASSINATURAVANCADA_VALIDAR_API_KEY="sk-8I4Y04vakqb993M70433s4K1p3LYJ4344rfo2gndQ3N96n8D"
                    JOB_MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL="http://200.152.47.34:8080/integraICP"
                    JOB_MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_CLEARINGS="/get-clearings"
                    JOB_MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_ASSINAR="/sign"

                    GITURL = "https://github.com/spbgovbr/sei-docker.git"
                    GITSEIADDRESS = params.gitSeiAddress
                    GITCRED = params.gitSeiKeyJenkins
                    GITSEIVERSAO = params.versaoSei
                    GITSEIKEY = params.gitSeiKey

                    MULTIORGAO = params.multiorgao
                    MULTIORGAOSIGLAS = (MULTIORGAO == 'true' ? params.multiorgaoSiglas : "");
                    MULTIORGAONOMES = (MULTIORGAO == 'true' ? params.multiorgaoNomes : "");
                    FEDERACAO = params.federacao

                    MODULOESTATISTICA_INSTALAR = params.moduloEstatisticaInstalar
                    MODULOESTATISTICA_VERSAO = params.moduloEstatisticaVersao
                    MODULOESTATISTICA_URL = params.moduloEstatisticasUrl
                    MODULOESTATISTICA_SIGLA = params.moduloEstatisticaSigla
                    MODULOESTATISTICA_CHAVE = params.moduloEstatisticaChave

                    MODULOPEN_INSTALAR = params.moduloPenInstalar
                    MODULOPEN_VERSAO = params.moduloPenVersao
                    MODULOPEN_ENDPOINT = params.moduloPenEndpoint
                    MODULOPEN_CONFIGURAR = params.moduloPenConfigurar
                    MODULOPEN_CERT = params.moduloPenCert
                    MODULOPEN_CERTSENHA = params.moduloPenCertSenha
                    MODULOPEN_GEARMAN_IP = params.moduloPenGearmanIp
                    MODULOPEN_GEARMAN_PORTA = params.moduloPenGearmanPorta
                    MODULOPEN_REPOSITORIOORIGEM = params.moduloPenRepositorioOrigem
                    MODULOPEN_TIPOPROCESSO = params.moduloPenTipoProcessoExterno
                    MODULOPEN_UNIDADEGERADORA = params.moduloPenUnidadeGeradora
                    MODULOPEN_UNIDADEASSOCIACAOSEI = params.moduloPenUnidadeAssociacaoSei
                    MODULOPEN_UNIDADEASSOCIACAOPEN = params.moduloPenUnidadeAssociacaoPen

                    MODULOPI_INSTALAR = params.moduloPIInstalar
                    MODULOPI_VERSAO = params.moduloPIVersao
                    MODULOPI_EMAIL = params.moduloPIemail
                    MODULOPI_URL = JOB_MODULOPI_URL
                    MODULOPI_USUARIO = JOB_MODULOPI_USUARIO
                    MODULOPI_SENHA = JOB_MODULOPI_SENHA

                    MODULOREST_INSTALAR = params.moduloRestInstalar
                    MODULOREST_VERSAO = params.moduloRestVersao
                    MODULOREST_URLNOTIFICACAO = JOB_MODULOREST_URLNOTIFICACAO
                    MODULOREST_IDAPP = JOB_MODULOREST_IDAPP
                    MODULOREST_CHAVE = JOB_MODULOREST_CHAVE
                    MODULOREST_TOKEN = JOB_MODULOREST_TOKEN

                    MODULOLOGINUNICO_INSTALAR = params.moduloLoginUnicoInstalar
                    MODULOLOGINUNICO_VERSAO = params.moduloLoginUnicoVersao
                    MODULOLOGINUNICO_CLIENTID = JOB_MODULOLOGINUNICO_CLIENTID
                    MODULOLOGINUNICO_SECRET = JOB_MODULOLOGINUNICO_SECRET
                    MODULOLOGINUNICO_URLPROVIDER = JOB_MODULOLOGINUNICO_URLPROVIDER
                    MODULOLOGINUNICO_REDIRECTURL = JOB_MODULOLOGINUNICO_REDIRECTURL
                    MODULOLOGINUNICO_URLLOGOUT = JOB_MODULOLOGINUNICO_URLLOGOUT
                    MODULOLOGINUNICO_SCOPE = JOB_MODULOLOGINUNICO_SCOPE
                    MODULOLOGINUNICO_SERVICOS = JOB_MODULOLOGINUNICO_SERVICOS
                    MODULOLOGINUNICO_REVALIDACAO = JOB_MODULOLOGINUNICO_REVALIDACAO
                    MODULOLOGINUNICO_CLIENTIDVALIDACAO = JOB_MODULOLOGINUNICO_CLIENTIDVALIDACAO
                    MODULOLOGINUNICO_SECRETVALIDACAO = JOB_MODULOLOGINUNICO_SECRETVALIDACAO
                    MODULOLOGINUNICO_ORGAO = JOB_MODULOLOGINUNICO_ORGAO

                    MODULOASSINATURA_INSTALAR = params.moduloAssinaturaInstalar
                    MODULOASSINATURA_VERSAO = params.moduloAssinaturaVersao
                    MODULOASSINATURA_CLIENTID = JOB_MODULOASSINATURA_CLIENTID
                    MODULOASSINATURA_SECRET = JOB_MODULOASSINATURA_SECRET
                    MODULOASSINATURA_URLPROVIDER = JOB_MODULOASSINATURA_URLPROVIDER
                    MODULOASSINATURA_URLSERVICOS = JOB_MODULOASSINATURA_URLSERVICOS

                    MODULO_ASSINATURAVANCADA_VALIDAR_API_URL=JOB_MODULO_ASSINATURAVANCADA_VALIDAR_API_URL
                    MODULO_ASSINATURAVANCADA_VALIDAR_API_KEY=JOB_MODULO_ASSINATURAVANCADA_VALIDAR_API_KEY
                    MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL=JOB_MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL
                    MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_CLEARINGS=JOB_MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_CLEARINGS
                    MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_ASSINAR=JOB_MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_ASSINAR

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

        stage('Checkout-SEI'){

            steps {

                dir('sei'){

                    sh """
                    git config --global http.sslVerify false
                    """

                    git branch: 'main',
                        credentialsId: GITCRED,
                        url: GITSEIADDRESS

                    sh """
                    echo "" > ../envstageanterior.env
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

                    withCredentials([ string(credentialsId: "github_pat_readonly_pengovbr", variable: 'LHAVE')]) {

                        sh """
                        cd infra
                        echo "export GITUSER_REPO_MODULOS=marlinhares" >> envlocal.env
                        echo "export GITPASS_REPO_MODULOS=${LHAVE}" >> envlocal.env
                        """
                    }

                    sh """
                    cd infra
                    echo "" >> envlocal.env
                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env
                    echo "export APP_MAIL_SERVIDOR=relay.nuvem.gov.br" >> envlocal.env
                    echo "export APP_HOST=${JOB_URL}" >> envlocal.env
                    echo "export APP_ORGAO=${JOB_ORGAO}" >> envlocal.env
                    echo "export APP_FONTES_GIT_PATH=git@github.com:supergovbr/super" >> envlocal.env
                    echo "export APP_FONTES_GIT_CHECKOUT=${GITSEIVERSAO}" >> envlocal.env
                    echo "export KUBERNETES_NAMESPACE=${JOB_NS}" >> envlocal.env
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

                    echo "export APP_ORGAOS_ADICIONAIS_SIGLA=${MULTIORGAOSIGLAS}" >> envlocal.env
                    echo "export APP_ORGAOS_ADICIONAIS_NOME=${MULTIORGAONOMES}" >> envlocal.env
                    echo "export APP_FEDERACAO_HABILITAR=${FEDERACAO}" >> envlocal.env

                    echo "export MODULO_ESTATISTICAS_INSTALAR=${MODULOESTATISTICA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_VERSAO=${MODULOESTATISTICA_VERSAO}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_SIGLA=${MODULOESTATISTICA_SIGLA}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_CHAVE=${MODULOESTATISTICA_CHAVE}" >> envlocal.env
                    echo "export MODULO_ESTATISTICAS_URL=${MODULOESTATISTICA_URL}" >> envlocal.env


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

                    echo "export MODULO_PEN_WEBSERVICE=https://homolog.api.processoeletronico.gov.br/interoperabilidade/${MODULOPEN_ENDPOINT}/v3/" >> envlocal.env

                    """

                    script {
                        if (MODULOPEN_CONFIGURAR == "false" ){

                            sh """

                            cd infra
                            echo "export MODULO_PEN_REPOSITORIO_ORIGEM=" >> envlocal.env
                            echo "export MODULO_PEN_TIPO_PROCESSO_EXTERNO=" >> envlocal.env
                            echo "export MODULO_PEN_UNIDADE_GERADORA=" >> envlocal.env
                            echo "export MODULO_PEN_UNIDADE_ASSOCIACAO_PEN=" >> envlocal.env
                            echo "export MODULO_PEN_UNIDADE_ASSOCIACAO_SEI=" >> envlocal.env
                            """

                        }
                    }

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

                        """
                    }

                    withCredentials([ string(credentialsId: MODULOPI_SENHA, variable: 'LHAVE')]) {

                        sh """

                        cd infra
                        echo "export MODULO_PI_SENHA=${LHAVE}" >> envlocal.env

                        """
                    }

                    sh """

                    cd infra

                    echo "export MODULO_REST_INSTALAR=${MODULOREST_INSTALAR}" >> envlocal.env
                    echo "export MODULO_REST_VERSAO=${MODULOREST_VERSAO}" >> envlocal.env
                    echo "export MODULO_REST_URL_NOTIFICACAO=${MODULOREST_URLNOTIFICACAO}" >> envlocal.env
                    echo "export MODULO_REST_ID_APP=${MODULOREST_IDAPP}" >> envlocal.env
                    echo "export MODULO_REST_TOKEN_SECRET=${MODULOREST_TOKEN}" >> envlocal.env
                    """

                    withCredentials([ string(credentialsId: MODULOREST_CHAVE, variable: 'LHAVE')]) {

                        sh """

                        cd infra
                        echo "export MODULO_REST_CHAVE=${LHAVE}" >> envlocal.env

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

                    echo "export MODULO_ASSINATURAVANCADA_VALIDAR_API_URL=${MODULO_ASSINATURAVANCADA_VALIDAR_API_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_VALIDAR_API_KEY=${MODULO_ASSINATURAVANCADA_VALIDAR_API_KEY}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL=${MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_CLEARINGS=${MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_CLEARINGS}" >> envlocal.env
                    echo "export MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_ASSINAR=${MODULO_ASSINATURAVANCADA_INTEGRA_ICP_URL_ASSINAR}" >> envlocal.env

                    """



                    sh """

                    cd infra

                    dcomp=""
                    if [ -d "../../sei/src" ]; then
                        dcomp="src/"
                    fi

                    set +e
                    grep -e "const SEI_VERSAO = '5\\..*\\..*';" ../../sei/\${dcomp}sei/web/SEI.php
                    e=\$?
                    set -e


                    if [ "\$e" = "0" ]; then

                        cat envlocal-example-mysql-sei5.env >> envlocal.env
                        echo "export DOCKER_IMAGE_BD=processoeletronico/mariadb10.5-sei50:latest" >> envlocal.env

                    fi

                    echo "export KUBERNETES_PVC_STORAGECLASS=nfs-client2" >> envlocal.env


                    make kubernetes_montar_yaml
                    make kubernetes_delete || true

                    make kubernetes_montar_yaml
                    make kubernetes_apply

                    sleep 20
                    kubectl -n ${JOB_NS} scale --replicas=0 deployment/jod || true
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