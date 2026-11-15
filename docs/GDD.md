# Mecha-Tech — Documento de Design (GDD)

> Mod Fabric · Minecraft 26.1.2 · Java 25
> Status: rascunho v0.1. Tudo aqui pode mudar.

## 1. Visão geral

Mecha-Tech é um mod de progressão tecnológica que termina em **mechas pilotáveis, modulares e customizáveis**.
O jogador começa com ferramentas mecânicas. Depois passa para uma **exo-armadura** com buffs e, por fim, constrói **mechas** peça por peça.

**Pilares**

1. **Modularidade:** toda armadura e todo mecha é feito de peças trocáveis, cada uma com seus atributos.
2. **Customização:** altura, tamanho (escala), cor e equipamento de cada parte.
3. **Construção como espetáculo:** a montagem do mecha é animada numa máquina grande.
4. **Progressão clara:** cada tier exige os minérios e as máquinas do tier anterior.

---

## 2. Progressão (tiers)

| Tier | Nome        | O que libera                                        | Máquina-chave              |
| ---- | ----------- | --------------------------------------------------- | -------------------------- |
| 0    | Mecânico    | Ferramentas mecânicas (broca, serra, chave inglesa) | Bancada Mecânica           |
| 1    | Exo         | Exo-armadura modular com buffs                      | Cabine de Calibração       |
| 2    | Mecha Leve  | Mechas pequenos (~3 blocos)                         | Forja de Peças + Montadora |
| 3    | Mecha Médio | Mechas de ~5 blocos, armas pesadas                  | Montadora (upgrade)        |
| 4    | Titã        | Mechas de ~8+ blocos, núcleo de Éter                | Montadora (upgrade final)  |

---

## 3. Minérios e materiais

### Minérios novos

| Minério             | Onde                                   | Altura           | Uso principal                            |
| ------------------- | -------------------------------------- | ---------------- | ---------------------------------------- |
| **Titânio**         | Overworld (também na versão deepslate) | Y -32 a 48       | Estrutura base, placas, ferramentas T0   |
| **Neodímio**        | Overworld, deepslate                   | Y -64 a 0 (raro) | Ímãs → servomotores e atuadores          |
| **Cobalto Rúbeo**   | Nether                                 | qualquer altura  | Ligas resistentes ao calor, blindagem T3 |
| **Cristal de Éter** | End, em geodos nas ilhas externas      | —                | Núcleos de energia de alto nível (T4)    |

### Materiais processados

- **Lingote / Placa de Titânio**
- **Liga de Titânio-Aço** (titânio + ferro na Fundição de Ligas)
- **Ímã de Neodímio** → **Servomotor** (ímã + cobre + placa)
- **Fio de Cobre Revestido** e **Circuito** (cobre + redstone + quartzo)
- **Placa Blindada** (liga + cobalto rúbeo)
- **Célula de Energia** (recarregável)
- **Núcleo de Energia**: Básico → Reforçado → de Éter

---

## 4. Energia

Sistema próprio e simples: **Energia Mecânica (EM)**.

- **Gerador a Combustão:** queima carvão ou lava e gera EM.
- **Painel / Gerador de Éter** (late game).
- As máquinas ficam com EM armazenada. A armadura e os mechas usam **Células de Energia** onde pode ser recarregada em qualquer gerador ou um **Núcleo de Energia**.
- Opcional no futuro: compatibilidade com Team Reborn Energy (padrão Fabric).

---

## 5. Blocos e máquinas

### 5.1 Bancada Mecânica (T0)

- Bloco 1×1. Interface de crafting própria (grade 4×4).
- Faz ferramentas, componentes básicos e as outras máquinas.

### 5.2 Fundição de Ligas

- Bloco 1×1. Usa EM. Combina 2 entradas → 1 liga.

### 5.3 Cabine de Calibração (T1, armadura)

- Bloco **1×2 de altura**, o jogador entra nele.
- Primeiro se coloca a **Carcaça da Exo-armadura** (item feito na Bancada).
- Lá dentro abre a **interface de customização**:
  - Visualização 3D do jogador com a armadura
  - Slots de módulo por peça (capacete, peitoral, pernas, botas)
  - Pintura (cores primária e secundária)
- Ao sair, o jogador está vestindo a armadura.

**Módulos da exo-armadura (exemplos)**
| Peça | Módulos |
|---|---|
| Capacete | Visão noturna, HUD de energia, respiração aquática |
| Peitoral | Força, escudo de energia, resistência |
| Pernas | Velocidade, passo alto (sobe 1 bloco) |
| Botas | Pulo duplo, amortecedor de queda, mini-jato |

A armadura gasta energia enquanto os módulos estão ativos.

### 5.4 Forja de Peças (T2, multibloco 2×2×2)

- Estrutura de 8 blocos com um **Controlador** em uma das faces.
- Cria **uma peça de mecha por vez** (cabeça, torso, braço, perna, mochila).
- Na interface você escolhe o **tipo de peça**, a **variante** e os **parâmetros** (tamanho e altura dentro de uma faixa).
- O resultado é um item-peça com seus dados salvos (data components).

### 5.5 Montadora de Mecha (T2+, multibloco grande)

- Estrutura de cerca de **7×7×9** com um Controlador e suportes/braços robóticos.
- As peças são colocadas nos slots (torso, cabeça, 2 braços, pernas, mochila, núcleo).
- **Animação de montagem:**
  1. Os braços robóticos pegam o torso e o posicionam no centro
  2. Encaixam pernas, braços e cabeça, com faíscas, partículas e som
  3. Instalam o núcleo e o mecha "liga" (os olhos acendem)
  4. A estrutura abre e o mecha vira uma entidade pronta para pilotar
- Também serve de **hangar**: repara, recarrega e troca peças de um mecha já montado.
- Upgrades da estrutura liberam os tiers 3 e 4 (mais altura e peças maiores).

### 5.6 Estação de Recarga

- Bloco simples para recarregar mechas em campo.

---

## 6. Mechas

### 6.1 Estrutura modular

| Slot            | Função                               | Exemplos de variante                         |
| --------------- | ------------------------------------ | -------------------------------------------- |
| **Torso**       | Cockpit e base, define o peso máximo | Leve, Padrão, Blindado                       |
| **Cabeça**      | Sensores: visão, radar, zoom         | Sensor básico, Radar, Visão térmica          |
| **Braço E / D** | Ferramenta ou arma                   | Punho, Broca, Serra, Canhão, Garra, Escudo   |
| **Pernas**      | Locomoção                            | Bípede, Quadrúpede, Esteira, Pernas de salto |
| **Mochila**     | Utilidade                            | Jato, Armazenamento, Bateria extra           |
| **Núcleo**      | Energia                              | Básico, Reforçado, Éter                      |

### 6.2 Atributos de cada peça

`peso`, `blindagem`, `consumo de energia`, `velocidade`, `força`, `escala`.
O mecha soma os atributos. Se passar do peso máximo do torso, ele fica lento ou não liga.

### 6.3 Customização

- **Escala / altura** por peça (ex.: pernas mais longas deixam o mecha mais alto e mais rápido, mas mais pesado)
- **Cor** primária, secundária e de luzes
- **Troca de peças** a qualquer momento na Montadora

### 6.4 Arquétipos (pré-montagens sugeridas)

- **Batedor:** leve, rápido, com jato
- **Minerador:** brocas nos dois braços e esteira
- **Combatente:** canhão + escudo, torso blindado
- **Construtor:** garras, coloca e quebra blocos em área
- **Titã (T4):** enorme, núcleo de Éter, armas pesadas

### 6.5 Pilotagem

- Clique direito no mecha para entrar no cockpit (câmera em 3ª pessoa por padrão).
- WASD para andar, espaço para pular ou usar o jato, clique esq./dir. para o braço esquerdo/direito.
- HUD com energia, integridade de cada peça e arma ativa.
- Peças podem ser danificadas separadamente. Um braço destruído para de funcionar.

---

## 7. Abordagem técnica (resumo)

- **Loader:** Fabric + Fabric API (projeto já configurado para 26.1.2).
- **Modelos:** feitos no Blockbench.
  - Itens e blocos: modelos JSON padrão
  - Mecha e partes: um modelo de entidade por peça, montados em tempo de execução e com escala aplicada por peça
  - Animações (andar, montagem): GeckoLib, se houver versão para 26.1.2 (precisa verificar), ou animação em código própria
- **Mecha:** uma `Entity` própria (montável), com as peças guardadas como dados (NBT/components) e hitbox calculada a partir da escala.
- **Multiblocos:** validação de estrutura no Controlador e renderização da animação via `BlockEntityRenderer`.
- **Interface da cabine:** Screen própria com preview 3D da entidade.
- **Mundo:** minérios via worldgen data-driven (configured/placed features).

> Observação: o projeto ainda usa o id/pacote de exemplo (`com.example`, `modid`, "mecha Armors King"). Sugestão: renomear para `mechatech` / `com.markin.mechatech`.

---

## 8. Roadmap de implementação

1. **Base:** renomear o projeto, registro de itens/blocos, aba criativa
2. **Minérios e materiais:** 4 minérios, worldgen, lingotes, placas, ligas
3. **T0:** Bancada Mecânica + ferramentas
4. **Energia:** gerador, cabos simples, Fundição de Ligas
5. **T1:** Carcaça, Cabine de Calibração, módulos da armadura
6. **T2 (peças):** multibloco Forja de Peças + itens-peça com atributos
7. **T2 (mecha):** entidade, pilotagem, renderização modular
8. **Montadora:** multibloco + animação de montagem
9. **T3/T4:** peças avançadas, armas, Titã
10. **Polimento:** sons, partículas, livro-guia, balanceamento

---

## 9. Perguntas em aberto

- Os mechas podem ser usados por outros jogadores (multiplayer) ou ter "dono"? vc tem um modulo que pode trancar mas de base qualquer um pode usar
- O mecha deve quebrar blocos ou causar dano ao mundo? (combate x construção) sim
- Vai ter inimigos novos (ex.: mechas hostis ou chefes) para justificar o combate? sim, com eles vc pode coletar peças pra melhoria de mecha tbm, quando ele for derrotado ele cai e vc pode desmontar ele(com um puzzle que pode quebrar as pelas)
- Energia própria ou compatível com outros mods tech? propria mas que funcione com qualquer outro mod de energia
- Os nomes dos minérios estão bons ou você prefere outros? gostei
