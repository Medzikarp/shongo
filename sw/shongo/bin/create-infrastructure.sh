CONTROLLER=127.0.0.1
cd `dirname $0`

./client-cli.sh --connect $CONTROLLER --testing-access-token --scripting <<EOF

    create-resource {
        class: 'Resource',
        name: 'namingService',
        description: 'Naming service for all technologies',
        allocatable: 1,
        capabilities: [{
            class: 'ValueProviderCapability',
            valueProvider: {
                class: 'ValueProvider.Pattern',
                patterns: ['ZZ-shongo-{hash}'],
                allowAnyRequestedValue: 1,
            },
        }]
    }

    create-resource {
        class: 'DeviceResource',
        name: 'mcu-cesnet',
        description: 'H.323/SIP MCU at CESNET',
        allocatable: 1,
        maximumFuture: 'P4M',
        technologies: ['H323','SIP'],
        address: '195.113.222.60',
        mode: {
            connectorAgentName: 'mcu-cesnet'
        },
        capabilities: [{
            class: 'RoomProviderCapability',
            licenseCount: 20,
            requiredAliasTypes: ['ROOM_NAME', 'H323_E164'],
        },{
            class: 'AliasProviderCapability',
            valueProvider: '1',
            aliases: [
                { type: 'ROOM_NAME', value: '{value}' },
                { type: 'SIP_URI', value: '{value}@cesnet.cz' }
            ],
            maximumFuture: 'P1Y',
            restrictedToResource: 1,
        },{
            class: 'AliasProviderCapability',
            valueProvider: {
                class: 'ValueProvider.Pattern',
                patterns: ['{digit:2}'],
            },
            aliases: [
                { type: 'H323_E164', value: '9500872{value}' },
                { type: 'H323_URI', value: '9500872{value}@{device.address}' },
                { type: 'H323_IP', value: '195.113.222.60 2{value}#' },
                { type: 'SIP_IP', value: '195.113.222.60 2{value}#' },
                { type: 'SIP_URI', value: '9500872{value}@cesnet.cz' }
            ],
            maximumFuture: 'P1Y',
            restrictedToResource: 1,
        }],
        administrators: [
            { class: 'OtherPerson', name: 'Martin Srom', email: 'srom.martin@gmail.com'},
            { class: 'OtherPerson', name: 'Jan Ruzicka', email: 'janru@cesnet.cz'},
            { class: 'OtherPerson', name: 'Milos Liska', email: 'xliska@fi.muni.cz'}
        ]
    }

    create-resource {
        class: 'DeviceResource',
        name: 'mcu-muni',
        description: 'H.323/SIP MCU at MUNI',
        allocatable: 1,
        maximumFuture: 'P4M',
        technologies: ['H323','SIP'],
        address: '147.251.15.253',
        mode: {
            connectorAgentName: 'mcu-muni'
        },
        capabilities: [{
            class: 'RoomProviderCapability',
            licenseCount: 10,
            requiredAliasTypes: ['ROOM_NAME', 'H323_E164'],
        }]
    }

    create-resource {
        class: 'DeviceResource',
        name: 'connect-cesnet',
        description: 'Adobe Connect server at CESNET',
        allocatable: 1,
        maximumFuture: 'P4M',
        address: 'https://actest-w3.cesnet.cz',
        technologies: ['ADOBE_CONNECT'],
        mode: {
            connectorAgentName: 'connect-cesnet'
        },
        capabilities: [{
            class: 'RoomProviderCapability',
            licenseCount: 10,
            requiredAliasTypes: ['ROOM_NAME'],
        },{
            class: 'AliasProviderCapability',
            valueProvider: {
                class: 'ValueProvider.Filtered',
                type: 'CONVERT_TO_URL',
                valueProvider: '1',
            },
            aliases: [
                { type: 'ROOM_NAME', value: '{requested-value}' },
                { type: 'ADOBE_CONNECT_URI', value: '{device.address}/{value}' }
            ],
            maximumFuture: 'P1Y',
            permanentRoom: 1,
        }],
        administrators: [
            { class: 'OtherPerson', name: 'Martin Srom', email: 'srom.martin@gmail.com'},
            { class: 'OtherPerson', name: 'Jan Ruzicka', email: 'janru@cesnet.cz'},
            { class: 'OtherPerson', name: 'Milos Liska', email: 'xliska@fi.muni.cz'}
        ]
    }

    create-resource {
        class: 'DeviceResource',
        name: 'c90-sitola',
        description: 'Tandberg endpoint in SITOLA at FI MUNI',
        allocatable: 1,
        technologies: ['H323'],
        mode: {
            connectorAgentName: 'c90-sitola'
        },
        capabilities: [{
            class: 'StandaloneTerminalCapability',
            aliases: [{
                type: 'H323_E164',
                value: '950081038'
            }]
        }]
    }

    create-resource {
        class: 'DeviceResource',
        name: 'lifesize-sitola',
        description: 'LifeSize endpoint in SITOLA at FI MUNI',
        allocatable: 1,
        technologies: ['H323'],
        mode: {
            connectorAgentName: 'lifesize-sitola'
        },
        capabilities: [{
            class: 'StandaloneTerminalCapability'
        }]
    }

EOF