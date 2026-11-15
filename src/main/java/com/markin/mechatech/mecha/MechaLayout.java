package com.markin.mechatech.mecha;

/**
 * Onde cada peça fica no corpo do mecha (em blocos, origem = pés, frente = -Z).
 * Usado pelo desenho (cliente) e pela posição do piloto/câmera (servidor).
 */
public final class MechaLayout {
	public static final float UNIT = MechaData.BASE_HEIGHT / 2.6f;
	public final float sL, sT, sH, sA, sA2, sB;
	public final float legs, torso, torsoBottom, torsoTop, head, headY, armL, armR, armY_L, armY_R, armX_L, armX_R, pack, packZ;

	public MechaLayout(MechaData d) {
		sL = MechaData.present(d.legs()) ? d.legs().scale() / 100f : 1f;
		sT = MechaData.present(d.torso()) ? d.torso().scale() / 100f : 1f;
		sH = d.head().scale() / 100f;
		sA = d.armLeft().scale() / 100f;
		sA2 = d.armRight().scale() / 100f;
		sB = d.backpack().scale() / 100f;
		legs = UNIT * sL;
		torso = UNIT * sT;
		torsoBottom = legs * 0.9f;
		torsoTop = torsoBottom + torso;
		head = UNIT * 0.8f * sH;
		headY = torsoTop - torso * 0.08f + head * 0.5f;
		armL = UNIT * sA;
		armR = UNIT * sA2;
		armY_L = torsoTop - torso * 0.05f - armL * 0.5f;
		armY_R = torsoTop - torso * 0.05f - armR * 0.5f;
		armX_L = -(0.33f * torso + 0.25f * armL);
		armX_R = 0.33f * torso + 0.25f * armR;
		pack = UNIT * 0.9f * sB;
		packZ = 0.3f * torso + 0.3f * pack;
	}

	/** Altura dos olhos (a câmera do piloto fica dentro da cabeça). */
	public float eyeY() {
		return headY + head * 0.05f;
	}
}
