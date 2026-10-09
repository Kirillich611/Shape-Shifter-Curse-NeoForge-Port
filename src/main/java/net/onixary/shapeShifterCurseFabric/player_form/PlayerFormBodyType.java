package net.onixary.shapeShifterCurseFabric.player_form;

/**
 * 玩家形态的体型类型，决定渲染和碰撞箱的工作模式。
 * <p>
 * 影响范围包括：GeoModel 骨骼动画的驱动方式、披风位置偏移、Entity Model Features（EMF）的动画行为、以及部分形态碰撞箱调整。
 */
public enum PlayerFormBodyType {
	/**
	 * 标准人形。使用原版玩家骨骼映射，RenderPlayer 常规渲染路径，EMF 动画照常播放。
	 */
	NORMAL,
	/** 野兽形态。使用四足或非人形模型，通过 SSC 自定义动画系统驱动，EMF 动画会被暂停以避免冲突。 */
    FERAL
}
