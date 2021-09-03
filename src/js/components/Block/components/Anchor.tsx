import styled from 'styled-components';

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
    vector-effect: non-scaling-stroke;
    pointer-events: none;
    transform: scale(1.001); // Prevents the bullet being squished
    overflow: visible; // Prevents the bullet being cropped
    width: 0.25em;
    height: 0.25em;
    color: var(--user-color, var(--body-text-color---opacity-low));
  }

  circle {
    fill: currentColor;
    transition: fill 0.05s ease, opacity 0.05s ease;
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
  }

  &:hover,
  &:hover:before,
  &:focus-visible:before {
    opacity: 1;
  }

  &.closed-with-children circle {
      stroke: var(--body-text-color);
      fill: var(--body-text-color---opacity-low);
      r: 2;
      stroke-width: 1;
      opacity: var(--opacity-med);
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

const Circle = () => (<svg viewBox="0 0 2 2">
  <circle cx="1" cy="1" r="2" />
</svg>)

const Dash = () => (<svg viewBox="0 0 1 1">
  <line x1="-1" y1="0" x2="1" y2="0" stroke="currentColor" strokeWidth="0.5" />
</svg>)

export interface AnchorProps {
  /**
   * What style of anchor to display
   */
  anchorElement: 'circle' | 'dash' | number;
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
}

/**
 * A handle and indicator of a block's position in the document
*/
export const Anchor = ({
  isClosedWithChildren,
  handlePressAnchor,
  handleAnchorContextMenu,
  anchorElement = 'circle',
  ...props
}: AnchorProps) => (
  <AnchorButton
    className={['anchor', isClosedWithChildren && 'closed-with-children'].join(' ')}
    draggable={true}
    onClick={handlePressAnchor}
    onContextMenu={handleAnchorContextMenu}
    onMouseOver={() => console.log('mouse over')}
    onMouseOut={() => console.log('mouse out')}
    onDragStart={() => console.log('drag start')}
    onDragEnd={() => console.log('drag end')}
    {...props}
  >
    {anchorElement === 'circle' && <Circle />}
    {anchorElement === 'dash' && <Dash />}
    {typeof anchorElement === 'number' && anchorElement}
  </AnchorButton>
);
