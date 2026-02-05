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
            name: 'gitSeiKeyJenkins',
            defaultValue:"github_pat_readonly_pengovbr",
            description: "Secret Jenkins para o Repo Github dos Fontes")
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
            name: 'moduloPenAmbiente',
            choices: ['https://dev.api.processoeletronico.gov.br', 'https://homolog.api.processoeletronico.gov.br', 'https://teste.api.processoeletronico.gov.br', 'https://api-tramita.hom.dataprev.gov.br'],
            description: 'Ambiente a ser utilizado')
        choice(
            name: 'moduloPenEndpoint',
            choices: ['rest/v4', 'rest/v3', 'soap/v3', 'soap/v4'],
            description: 'Antes de subir o ambiente, verifique se o módulo é compatível com SOAP ou REST e ajuste aqui')
        choice(
            name: 'moduloPenConfigurar',
            choices: ['true', 'false'],
            description: 'Caso deseje que o módulo confgure automaticamente para envio e recebimento. Se marcar falso, deverá configurar no menu de admin do módulo')
        choice(
            name: 'moduloPenCert',
            choices: ['dev certId: credModuloPenCertDevIN', 'homolog certId: credModuloPenCertDevFabio', 'teste certId: credModuloPenCertTesteIN', 'hom Dataprev certId: credModuloPenCertHomDtIN' ],
            description: 'Certificado base64 do módulo em jenkins secret')
        choice(
            name: 'moduloPenCertSenha',
            choices: ['dev Senha: credModuloPenCertSenhaDevIN', 'homolog Senha: credModuloPenCertSenhaDevFabio', 'teste Senha: credModuloPenCertSenhaTesteIN', 'hom Dataprev Senha: credModuloPenCertSenhaHomDtIN' ],
            description: "Senha do Certificado do módulo em jenkins secret")
        string(
            name: 'moduloPenGearmanIp',
            defaultValue:"127.0.0.1",
            description: "Caso queira usar gearman informe o endereco. Caso n queira deixe em branco")
        string(
            name: 'moduloPenGearmanPorta',
            defaultValue:"4730",
            description: "Caso queira usar gearman informe a porta. Caso n queira deixe em branco")
        choice(
            name: 'moduloPenRepositorioOrigem',
            choices: ['Dev Interno: Repo ID 28', 'Homolog Dataprev: Repo ID 65', 'Homolog: Repo ID 37', 'Teste: Repo ID 5', 'hom Dataprev: Repo ID 47'],
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
        choice(
            name: 'moduloPenUnidadeAssociacaoPen',
            choices: ['Dev Interno: Unidade ID 169075', 'Homolog: Unidade ID 208005', 'Teste: Unidade ID 192963', 'hom Dataprev: Unidade ID 118153'],
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
            choices: ['false', 'Aguardando Credencial'],
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
        choice(
            name: 'moduloIncomInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Assinatura Avançada')
        string(
            name: 'moduloIncomVersao',
            defaultValue:"master",
            description: "Versao do Módulo Incom")
        choice(
            name: 'moduloRespostaInstalar',
            choices: ['false', 'true'],
            description: 'Instalar Módulo Resposta')
        string(
            name: 'moduloRespostaVersao',
            defaultValue:"master",
            description: "Versão do Módulo Resposta")

    }

    stages {

        stage('Inicializar Job'){
            steps {

                script{

                    JOB_URL = "sei.incom.teste.processoeletronico.gov.br"
                    JOB_ORGAO = "IN"
                    JOB_NS = "sei-incom"

                    JOB_MODULOPI_URL = "https://protocolointegrado.preprod.nuvem.gov.br/ProtocoloWS/integradorService?wsdl"
                    JOB_MODULOPI_USUARIO = "credModuloPIINUsuario"
                    JOB_MODULOPI_SENHA = "credModuloPIINSenha"

                    JOB_MODULOREST_URLNOTIFICACAO = "https://app-push-gestao-api.dev.nuvem.gov.br/mba-mmmessage/message"
                    JOB_MODULOREST_IDAPP = "4"
                    JOB_MODULOREST_CHAVE = "credModWsSuperChave"
                    JOB_MODULOREST_TOKEN = "504CE1E9-8913-488F-AB3E-EDDABC065B06"

                    JOB_MODULOLOGINUNICO_CLIENTID = "credLoginUnicoClientId"
                    JOB_MODULOLOGINUNICO_SECRET = "credLoginUnicoSecret"
                    JOB_MODULOLOGINUNICO_URLPROVIDER = "https://sso.staging.acesso.gov.br/"
                    JOB_MODULOLOGINUNICO_REDIRECTURL = "https://sei.orgao6.tramita.processoeletronico.gov.br/sei/modulos/loginunico/controlador_loginunico.php"
                    JOB_MODULOLOGINUNICO_URLLOGOUT = "https://sei.orgao6.tramita.processoeletronico.gov.br/sei/modulos/loginunico/logout.php"
                    JOB_MODULOLOGINUNICO_SCOPE = "openid+email+phone+profile+govbr_empresa+govbr_confiabilidades"
                    JOB_MODULOLOGINUNICO_SERVICOS = "https://api.staging.acesso.gov.br/"
                    JOB_MODULOLOGINUNICO_REVALIDACAO = "https://oauth.staging.acesso.gov.br/v1/"
                    JOB_MODULOLOGINUNICO_CLIENTIDVALIDACAO = "sei.resposta.nuvem.gov.br/validacaosenha"
                    JOB_MODULOLOGINUNICO_SECRETVALIDACAO = "credLoginUnicoSecretValidacao"
                    JOB_MODULOLOGINUNICO_ORGAO = "0"

                    GITURL = "https://github.com/spbgovbr/sei-docker.git"
                    GITCREDFONTE = params.gitSeiKeyJenkins
                    GITSEIVERSAO = params.versaoSei

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
                    MODULOPEN_AMBIENTE = params.moduloPenAmbiente
                    MODULOPEN_ENDPOINT = params.moduloPenEndpoint
                    MODULOPEN_CONFIGURAR = params.moduloPenConfigurar
                    MODULOPEN_CERT = params.moduloPenCert.split(' certId: ')[1]
                    MODULOPEN_CERTSENHA = params.moduloPenCertSenha.split(' Senha: ')[1]
                    MODULOPEN_GEARMAN_IP = params.moduloPenGearmanIp
                    MODULOPEN_GEARMAN_PORTA = params.moduloPenGearmanPorta
                    MODULOPEN_REPOSITORIOORIGEM = params.moduloPenRepositorioOrigem.split('ID ')[1]
                    MODULOPEN_TIPOPROCESSO = params.moduloPenTipoProcessoExterno
                    MODULOPEN_UNIDADEGERADORA = params.moduloPenUnidadeGeradora
                    MODULOPEN_UNIDADEASSOCIACAOSEI = params.moduloPenUnidadeAssociacaoSei
                    MODULOPEN_UNIDADEASSOCIACAOPEN = params.moduloPenUnidadeAssociacaoPen.split('ID ')[1]

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

                    MODULOINCOM_INSTALAR = params.moduloIncomInstalar
                    MODULOINCOM_VERSAO = params.moduloIncomVersao

                    MODULORESPOSTA_INSTALAR = params.moduloRespostaInstalar
                    MODULORESPOSTA_VERSAO = params.moduloRespostaVersao

                    if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        warning('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

                }

                sh """
                echo ${WORKSPACE}
                ls -lha

                sudo rm -rf kube

                sudo rm -rf sei
                """
            }
        }

        stage('Baixar SEI.php'){

            steps {

                dir('sei'){

                    withCredentials([ string(credentialsId: GITCREDFONTE, variable: 'LHAVE')]) {

                        sh """

                        baixar_seiphp(){

                            if [ ! -f SEI.php ]; then

                                curl -H 'Authorization: token ${LHAVE}' \
                                     -H 'Accept: application/vnd.github.v3.raw' \
                                     -o SEI.php \
                                     -L https://api.github.com/repos/pengovbr/sei/contents/\${1}

                                set +e
                                grep -e "SEI_VERSAO" SEI.php
                                e=\$?
                                set -e

                                if [ ! "\$e" = "0" ]; then
                                    rm SEI.php
                                fi

                            fi
                        }

                        gettaghash(){

                            r=\$(curl -H 'Authorization: token ${LHAVE}' \
                                 https://api.github.com/repos/pengovbr/sei/git/refs/tags/\${1})

                            hash=\$(echo \$r | grep -oP 'git/tags/.*?"' | sed "s|git/tags/||g" | sed 's|"||g')
                            if [ "\$hash" = "" ]; then
                                echo "Versao nao encontrada no git"
                                exit 1
                            fi
                            echo "\$hash"


                        }


                        rm -rf SEI.php

                        baixar_seiphp "src/sei/web/SEI.php?ref=${GITSEIVERSAO}"
                        baixar_seiphp "sei/web/SEI.php?ref=${GITSEIVERSAO}"
                        if [ ! -f SEI.php ]; then
                            hash=\$(gettaghash "${GITSEIVERSAO}")
                            baixar_seiphp "src/sei/web/SEI.php?ref=\${hash}"
                            baixar_seiphp "sei/web/SEI.php?ref=\${hash}"
                        fi

                        if [ ! -f SEI.php ]; then
                            echo "Versao nao encontrada no repo git"
                            exit 1
                        fi

                        ls -lha
                        cat SEI.php

                        """
                    }
                }
            }
        }

        stage('Checkout-ProjetoKube'){

            steps {

                dir('kube'){

                    sh """
                    rm -rf infra/envlocal.env
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

                    withCredentials([ string(credentialsId: GITCREDFONTE, variable: 'LHAVE')]) {

                        sh """
                        cd infra
                        echo "export GITUSER_REPO_MODULOS=marlinhares" >> envlocal.env
                        echo "export GITPASS_REPO_MODULOS=${LHAVE}" >> envlocal.env
                        echo "export APP_FONTES_GITHUB_TOKEN=${LHAVE}" >> envlocal.env
                        """
                    }

                    sh """
                    cd infra
                    echo "" >> envlocal.env
                    echo "export APP_MAIL_SERVIDOR=relay.nuvem.gov.br" >> envlocal.env
                    echo "export KUBERNETES_RESOURCES_INFORMAR=false" >> envlocal.env
                    echo "export APP_HOST=${JOB_URL}" >> envlocal.env
                    echo "export APP_ORGAO=${JOB_ORGAO}" >> envlocal.env
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

                    echo "export MODULO_PEN_WEBSERVICE=${MODULOPEN_AMBIENTE}/interoperabilidade/${MODULOPEN_ENDPOINT}/" >> envlocal.env

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


                    withCredentials([file(credentialsId: "moduloassinatura.properties", variable: 'FILE')]) {
                        sh "cp \$FILE moduloassinatura.properties; echo \$FILE "
                        sh "cat moduloassinatura.properties "
                    }

                    script {
                        def props = readProperties file: 'moduloassinatura.properties'

                        MODULOASSINATURA_URLPROVIDER = props['ASSINATURA_URL_PROVIDER']
                        MODULOASSINATURA_CLIENTID = props['ASSINATURA_CLIENT_ID']
                        MODULOASSINATURA_SECRET = props["ASSINATURA_SECRET"]
                        MODULOASSINATURA_VALIDAR_API_URL = props["VALIDAR_API_URL"]
                        MODULOASSINATURA_VALIDAR_API_KEY = props["VALIDAR_API_KEY"]
                        MODULOASSINATURA_TOKEN_URL = props["TOKEN_URL"]
                        MODULOASSINATURA_TOKEN_SIGN_URL = props["TOKEN_URL_ASSINAR"]
                        MODULOASSINATURA_INTEGRA_ICP_URL = props["INTEGRA_ICP_URL"]
                        MODULOASSINATURA_INTEGRA_ICP_URL_CLEARINGS = props["INTEGRA_ICP_URL_CLEARINGS"]
                        MODULOASSINATURA_INTEGRA_ICP_URL_ASSINAR = props["INTEGRA_ICP_URL_ASSINAR"]
                        MODULOASSINATURA_CLOUDPSC_URL = props["CLOUD_PSC_URL"]
                        MODULOASSINATURA_CLOUDPSC_START_URL = props["CLOUD_PSC_URL_START"]
                        MODULOASSINATURA_CLOUDPSC_SIGN_URL = props["CLOUD_PSC_URL_ASSINAR"]
                        MODULOASSINATURA_API_KEY_ITYHY = props["API_KEY_ITYHY"]

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

                    echo "export MODULO_ASSINATURA_INSTALAR=${MODULOASSINATURA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_VERSAO=${MODULOASSINATURA_VERSAO}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_URLPROVIDER=${MODULOASSINATURA_URLPROVIDER}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_CLIENTID=${MODULOASSINATURA_CLIENTID}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_SECRET=${MODULOASSINATURA_SECRET}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_VALIDAR_API_URL=${MODULOASSINATURA_VALIDAR_API_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_VALIDAR_API_KEY=${MODULOASSINATURA_VALIDAR_API_KEY}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_TOKEN_URL=${MODULOASSINATURA_TOKEN_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_TOKEN_SIGN_URL=${MODULOASSINATURA_TOKEN_SIGN_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_INTEGRA_ICP_URL=${MODULOASSINATURA_INTEGRA_ICP_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_INTEGRA_ICP_URL_CLEARINGS=${MODULOASSINATURA_INTEGRA_ICP_URL_CLEARINGS}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_INTEGRA_ICP_URL_ASSINAR=${MODULOASSINATURA_INTEGRA_ICP_URL_ASSINAR}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_CLOUDPSC_URL=${MODULOASSINATURA_CLOUDPSC_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_CLOUDPSC_START_URL=${MODULOASSINATURA_CLOUDPSC_START_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_CLOUDPSC_SIGN_URL=${MODULOASSINATURA_CLOUDPSC_SIGN_URL}" >> envlocal.env
                    echo "export MODULO_ASSINATURA_API_KEY_ITYHY=${MODULOASSINATURA_API_KEY_ITYHY}" >> envlocal.env


                    echo "export MODULO_INCOM_INSTALAR=${MODULOINCOM_INSTALAR}" >> envlocal.env
                    echo "export MODULO_INCOM_VERSAO=${MODULOINCOM_VERSAO}" >> envlocal.env

                    echo "export MODULO_RESPOSTA_INSTALAR=${MODULORESPOSTA_INSTALAR}" >> envlocal.env
                    echo "export MODULO_RESPOSTA_VERSAO=${MODULORESPOSTA_VERSAO}" >> envlocal.env

                    """




                    sh """

                    cd infra

                    set +e
                    grep -e "const SEI_VERSAO = '5\\..*\\..*';" ../../sei/SEI.php
                    e=\$?
                    set -e




                    if [ "\$e" = "0" ]; then

                        cat envlocal-example-mysql-sei5.env >> envlocal.env
                        echo "export DOCKER_IMAGE_BD=processoeletronico/mariadb10.5-sei50:latest" >> envlocal.env

                    fi

                    echo "export KUBERNETES_PVC_STORAGECLASS=nfs-client" >> envlocal.env

                    make kubernetes_montar_yaml
                    make kubernetes_delete || true

                    make kubernetes_montar_yaml

                    sed -i "s|imagePullPolicy: Always|imagePullPolicy: IfNotPresent|g" orquestrators/rancher-kubernetes/topublish/*.yaml
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