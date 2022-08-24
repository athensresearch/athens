import * as React from "react";
import { VStack, Box, Portal, AvatarGroup, Avatar } from "@chakra-ui/react";
import {
  DndContext,
  useSensors,
  closestCenter,
  PointerSensor,
  DragOverlay,
  useSensor
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
  arrayMove
} from "@dnd-kit/sortable";
import { Item, ItemDragOverlay } from "./Item";

// configuration
const activationConstraint = {
  distance: 15,
}


export const List = (props) => {
  const {
    items: outerItems,
    children,
    onUpdateItemsOrder,
    onOpenItem,
    ...rest
  } = props;
  const ids = outerItems.map(x => x.key)

  const container = React.useRef();
  const [containerWidth, setContainerWidth] = React.useState("unset");

  // Maintain an internal list of items for proper animation
  const [items, setItems] = React.useState(outerItems);
  const [activeId, setActiveId] = React.useState(null);

  React.useEffect(() => {
    setItems(outerItems)
  }, [outerItems])

  React.useEffect(() => {
    if (container.current) {
      const el = container.current as HTMLElement;
      setContainerWidth(el.getBoundingClientRect().width.toString());
    }
  }, [container]);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint }),
  );

  const handleDragStart = (e) => {
    setActiveId(e.active.id);
  };

  const handleDragEnd = (e) => {
    const { active, over } = e;
    setActiveId(null);

    if (active.id !== over.id) {
      setItems((items) => {
        const oldIndex = ids.indexOf(active.id);
        const newIndex = ids.indexOf(over.id);
        const newItems = arrayMove(items, oldIndex, newIndex);
        onUpdateItemsOrder(items[oldIndex].key, items[newIndex].key, oldIndex, newIndex);
        return newItems;
      });
    }
  };

  return (
    <Box sx={{ "--parentWidth": containerWidth }}>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
        onDragStart={handleDragStart}
      >
        <VStack spacing={0.25} align="stretch" overflowX="hidden" {...rest}>
          <SortableContext strategy={verticalListSortingStrategy} items={items}>
            {items.map(({ ...props }) => <Item
              {...props}
              ></Item>)}
          </SortableContext>
        </VStack>

        <Portal>
          <DragOverlay>
            {activeId ? <ItemDragOverlay sx={{ "--parentWidth": containerWidth }} key={activeId} id={activeId} /> : null}
          </DragOverlay>
        </Portal>
      </DndContext>
    </Box>
  );
};
