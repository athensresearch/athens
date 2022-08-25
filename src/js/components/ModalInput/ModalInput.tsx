import { Popover, PopoverProps } from "@chakra-ui/react";
import * as React from "react";


/**
 * Popperjs modifier to set the width of the popper to the width of the reference element.
 */
export const matchWidth = {
  name: "matchWidth",
  enabled: true,
  phase: "beforeWrite",
  requires: ["computeStyles"],
  fn: ({ state }) => {
    state.styles.popper.minWidth = `${state.rects.reference.width}px`;
  },
  effect: ({ state }) => {
    state.elements.popper.style.minWidth = `${state.elements.reference.offsetWidth}px`;
  }
};

/**
 * Popperjs modifier to override offset to place the popover overlapping the reference element
 */
export const inset = {
  name: "offset",
  options: {
    offset: ({ placement, reference }) => {
      switch (placement) {
        case "top":
          return [0, -reference.height];
        case "top-start":
          return [0, -reference.height];
        case "top-end":
          return [0, -reference.height];
        default:
        case "bottom":
          return [0, -reference.height];
        case "bottom-start":
          return [0, -reference.height];
        case "bottom-end":
          return [0, -reference.height];
        case "left":
          return [0, -reference.width];
        case "left-start":
          return [0, -reference.width];
        case "left-end":
          return [0, -reference.width];
        case "right":
          return [0, -reference.width];
        case "right-start":
          return [0, -reference.width];
        case "right-end":
          return [0, -reference.width];
      }
    }
  }
};

/**
 * Popperjs modifier to set the default popper container to "flex" layout
 */
export const containerIsFlex = {
  name: "containerIsFlex",
  enabled: true,
  phase: "beforeWrite",
  fn({ state }) {
    state.styles.popper = { ...state.styles.popper, display: "flex" };
  }
};


/** 
 * ModalInput
 * A modal for popover-style field interactions, where a
 * field is placed over the top of the reference element.
 * @param {React.ReactChildren} children 
 * @param {PopoverProps} PopoverProps
 * @returns {React.ReactNode}
 */
export const ModalInput = (props) => {
  const { children, ...popoverProps } = props;

  return (
    <Popover
      matchWidth
      flip={false}
      modifiers={[
        inset,
        matchWidth,
        containerIsFlex,
        {
          name: "preventOverflow",
          options: {
            altAxis: true,
            padding: 8,
            tetherOffset: 8
          }
        }
      ]}
      {...popoverProps}
    >
      {children}
    </Popover>
  );
};
