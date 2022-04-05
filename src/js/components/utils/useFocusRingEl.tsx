import React from "react";
import ReactDOM from "react-dom";
import styled from "styled-components";
import { useFocusRing } from "@react-aria/focus";
import { DOMRoot } from "@/utils/config";

const focusRingRectStyle = (ref): any => {
  const position = ref?.current?.getBoundingClientRect();
  const style = getComputedStyle(ref?.current);
  const borderTopLeftRadius = style.borderTopLeftRadius;
  const borderTopRightRadius = style.borderTopRightRadius;
  const borderBottomRightRadius = style.borderBottomRightRadius;
  const borderBottomLeftRadius = style.borderBottomLeftRadius;
  let inset = style.getPropertyValue("--focus-ring-inset");
  if (inset === "") {
    inset = "-3px";
  }

  return {
    "--inset": inset,
    "--left": position.left + "px",
    "--top": position.top + "px",
    "--width": position.width + "px",
    "--height": position.height + "px",
    "--border-top-left-radius": borderTopLeftRadius,
    "--border-top-right-radius": borderTopRightRadius,
    "--border-bottom-right-radius": borderBottomRightRadius,
    "--border-bottom-left-radius": borderBottomLeftRadius,
  };
};

const FocusRingEl = styled.div<React.HTMLAttributes<HTMLDivElement>>`
  position: absolute;
  inset: var(--inset);
  border: 2px solid var(--link-color);
  box-shadow: inset 0 0 0 1px var(--background-color);
  top: calc(var(--top) + var(--inset));
  left: calc(var(--left) + var(--inset));
  width: calc(var(--width) - var(--inset) * 2);
  height: calc(var(--height) - var(--inset) * 2);
  z-index: 99999;
  pointer-events: none;
  border-top-left-radius: calc(var(--border-top-left-radius) - var(--inset));
  border-top-right-radius: calc(var(--border-top-right-radius) - var(--inset));
  border-bottom-left-radius: calc(
    var(--border-bottom-left-radius) - var(--inset)
  );
  border-bottom-right-radius: calc(
    var(--border-bottom-right-radius) - var(--inset)
  );
`;

export const useFocusRingEl = (ref) => {
  const { isFocusVisible, focusProps } = useFocusRing(ref);

  const FocusRing = isFocusVisible ? (
    ReactDOM.createPortal(
      <FocusRingEl style={focusRingRectStyle(ref)} />,
      DOMRoot,
    )
  ) : (
    <></>
  );

  return {
    isFocusVisible,
    focusProps,
    FocusRing,
  };
};
