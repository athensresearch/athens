import React from "react";

import { useDroppable } from "@dnd-kit/core";

export function Droppable(props) {
  const { id, children } = props;
  const { isOver, setNodeRef } = useDroppable({
    id
  });

  return <div ref={setNodeRef}>{children(isOver)}</div>;
}
