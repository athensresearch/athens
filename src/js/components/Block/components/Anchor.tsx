import React from 'react'
import styled from 'styled-components';
import { useFocusRing } from '@react-aria/focus';
import { useTooltipTrigger, useTooltip } from '@react-aria/tooltip'
import { useTooltipTriggerState } from '@react-stately/tooltip';
import { TooltipTriggerProps } from '@react-types/tooltip';
import { mergeProps } from '@react-aria/utils';
import { DetailPopover } from '@/Block/components/DetailPopover';

export const AnchorButton = styled.button`
  flex-shrink: 0;
  grid-area: bullet;
  position: relative;
  z-index: 2;
  cursor: pointer;
  appearance: none;
  border: 0;
  background: transparent;
  transition: all 0.05s ease;
  color: inherit;
  margin-right: 0.25em;
  display: flex;
  place-items: center;
  place-content: center;
  padding: 0;
  height: 2em;
  width: 1em;

  svg {
    pointer-events: none;
    transform: scale(1.0001); // Prevents the bullet being squished
    overflow: visible; // Prevents the bullet being cropped
    width: 1em;
    height: 1em;
    color: var(--user-color, var(--body-text-color---opacity-low));

    * {
      vector-effect: non-scaling-stroke;
    }
  }

  circle {
    fill: currentColor;
    transition: fill 0.05s ease, opacity 0.05s ease;
  }

  &:focus {
    outline: none;
  }

  &:before {
    content: '';
    inset: 0.25rem -0.125rem;
    z-index: -1;
    transition: opacity 0.1s ease;
    position: absolute;
    border-radius: 0.25rem;
    opacity: 0;
    background: var(--background-plus-2);
    box-shadow: var(--depth-shadow-8);
  }

  &:hover {
    color: var(--link-color);
    z-index: 100;
  }

  &:hover,
  &:hover:before,
  &:focus-visible:before {
    opacity: 1;
  }

  &.closed-with-children {
    circle {
      stroke: var(--body-text-color);
      fill: var(--body-text-color---opacity-low);
      r: 5;
      stroke-width: 2px;
      opacity: var(--opacity-med);
    }
  }

  &:hover svg {
    transform: scale(1.3);
  }

  &.dragging {
    z-index: 1;
    cursor: grabbing;
    color: var(--body-text-color);
  }
`;

const anchorElements = {
  circle: <svg viewBox="0 0 24 24">
    <circle cx="12" cy="12" r="4" />
  </svg>,
  dash: <svg viewBox="0 0 1 1">
    <line x1="-1" y1="0" x2="1" y2="0" stroke="currentColor" strokeWidth="0.5" />
  </svg>
}

const FocusRing = styled.div`
  position: absolute;
  inset: 0.25rem -0.125rem;
  border: 2px solid var(--link-color);
  border-radius: 0.25rem;
`;

export interface AnchorProps extends TooltipTriggerProps {
  /**
   * What style of anchor to display
   */
  anchorElement?: 'circle' | 'dash' | number;
  /**
   * Whether block is closed and has children
   */
  isClosedWithChildren: boolean;
  /**
   * When the anchor button is pressed
   */
  handlePressAnchor: () => void;
  /**
   * When the anchor button is right-clicked
   */
  handleAnchorContextMenu?: () => void;
  block: any;
  shouldShowDebugDetails: boolean;
}

/**
 * A handle and indicator of a block's position in the document
*/
export const Anchor = (props: AnchorProps) => {
  const { isClosedWithChildren, anchorElement, shouldShowDebugDetails, block } = props;
  const ref = React.useRef();
  let state = useTooltipTriggerState(props);
  let { triggerProps, tooltipProps } = useTooltipTrigger({ delay: 500 }, state, ref);
  const { isFocusVisible, focusProps } = useFocusRing();

  return (
    <>
      <AnchorButton
        className={['anchor', isClosedWithChildren && 'closed-with-children'].join(' ')}
        ref={ref}
        draggable={true}
        {...props}
        {...mergeProps(focusProps, triggerProps)}
      >
        {anchorElements[anchorElement] || anchorElements['circle']}
        {isFocusVisible && <FocusRing />}
      </AnchorButton>
      {shouldShowDebugDetails && state.isOpen && <DetailPopover block={block} {...tooltipProps} state={state} />}
    </>
  )
};
