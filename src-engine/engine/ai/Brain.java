package engine.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import engine.core.Model;
import engine.core.Ticker;
import engine.entities.Bot;
import engine.entities.Entity;

// == BRAIN ==

/**
 * The AI scheduler: on each AI tick it wakes every {@link Bot} via
 * {@link Bot#tick(double)} so it can re-decide.
 *
 * <p>
 * {@code Brain} is the sibling of the motion {@link Ticker}: motion advances
 * the world ({@code Ticker -> Model.update()}), the brain advances the bots
 * ({@code Brain.onTick -> bot.tick()}).
 * 
 * <p>
 * It is itself a {@link Ticker.TickListener}, so it reuses the {@code Ticker}'s
 * drift-compensated loop: run it with {@code new Ticker(aiTps).start(brain)}.
 *
 */
public class Brain implements Ticker.TickListener {

	/**
	 * Bottom-to-top activation order (largest grid {@code y} first, since {@code y}
	 * grows downward). The deterministic order gravity needs: the lowest fallable
	 * entity decides before the ones stacked above it, so a column settles cleanly.
	 */
	public static final Comparator<Entity> BOTTOM_FIRST = Comparator.comparingInt((Entity e) -> -e.position().y())
			.thenComparingInt(e -> e.position().x());

	private final Model model;
	/**
	 * Optional deterministic activation order; {@code null} = unspecified (set)
	 * order.
	 */
	private Comparator<Entity> activationOrder;

	public Brain(Model model) {
		this.model = model;
	}

	/**
	 * Fix the order in which bots are woken each tick (e.g. {@link #BOTTOM_FIRST}
	 * for gravity). {@code null} restores the unspecified iteration order.
	 */
	public void setActivationOrder(Comparator<Entity> order) {
		this.activationOrder = order;
	}

	/**
	 * One AI tick: wake every entity's bot so it can re-decide. Iterates a snapshot
	 * so a {@code tick} that spawns or removes an entity cannot wreck the
	 * iteration; the snapshot is sorted by {@link #activationOrder} when one is
	 * set.
	 */
	@Override
	public void onTick(double deltaTime) {
		List<Entity> snapshot = new ArrayList<>(model.entities());
		if (activationOrder != null)
			snapshot.sort(activationOrder);
		for (Entity e : snapshot) {
			Bot bot = e.bot();
			if (bot != null) // inert props (e.g. a gum) have no bot
				bot.tick(deltaTime);
		}
	}
}
