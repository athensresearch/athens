import * as React from "react";
import { VStack, Portal } from "@chakra-ui/react";
import {
  DndContext,
  useSensors,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  DragOverlay,
  useSensor
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
  sortableKeyboardCoordinates,
  arrayMove
} from "@dnd-kit/sortable";
import { Item, ItemDragOverlay } from "./Item";

// configuration
const activationConstraint = {
  distance: 15,
}

export const List = (props) => {
  const { items, onUpdateItemsOrder, ...rest } = props;

  const [activeId, setActiveId] = React.useState(null);

  const handleDragStart = (e) => {
    setActiveId(e.active.id);
  };

  const handleDragEnd = (e) => {
    const { active, over } = e;

    setActiveId(null);

    if (active.id !== over.id) {
      onUpdateItemsOrder((items) => {
        const oldIndex = items.indexOf(active.id);
        const newIndex = items.indexOf(over.id);

        return arrayMove(items, oldIndex, newIndex);
      });
    }
  };

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates
    })
  );

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragEnd={handleDragEnd}
      onDragStart={handleDragStart}
    >
      <VStack align="stretch" {...rest}>
        <SortableContext strategy={verticalListSortingStrategy} items={items}>
          {items.map((item) => (
            <Item key={item} id={item} />
          ))}
        </SortableContext>
      </VStack>

      <Portal><DragOverlay>
        {activeId ? <ItemDragOverlay key={activeId} id={activeId} /> : null}
      </DragOverlay>
      </Portal>
    </DndContext>
  );
};
