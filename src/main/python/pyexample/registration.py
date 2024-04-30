import com.ultreon.quantum.block.Block as Block
import com.ultreon.quantum.item.BlockItem as BlockItem
import com.ultreon.quantum.item.Item as Item
import com.ultreon.quantum.registry.DeferRegistry as DeferRegistry
import com.ultreon.quantum.registry.Registries as Registries

from pyexample.constants import MOD_ID


class Blocks:
    @staticmethod
    def init():
        Blocks.REGISTER.register()

    REGISTER: DeferRegistry = DeferRegistry.of(MOD_ID, Registries.BLOCK)

    EXAMPLE_BLOCK = REGISTER.defer("example_block", lambda: Block())

    REGISTER.register()


class Items:
    @staticmethod
    def init():
        Items.REGISTER.register()

    REGISTER: DeferRegistry = DeferRegistry.of(MOD_ID, Registries.ITEM)

    EXAMPLE_ITEM = REGISTER.defer(
        "example_item", lambda: BlockItem(Item.Properties(), lambda: Blocks.EXAMPLE_BLOCK.get())
    )

    REGISTER.register()
