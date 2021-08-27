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
  margin-right: 0.25em;
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
      r: 1.5;
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

const Circle = () => (<svg viewBox="0 0 1 1">
  <circle cx="0.5" cy="0." r="1" />
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
}

export const Anchor = ({
  isClosedWithChildren,
  handlePressAnchor,
  anchorElement = 'circle',
  ...props
}: AnchorProps) => (
  <AnchorButton
    className={['anchor', isClosedWithChildren && 'closed-with-children'].join(' ')}
    tabIndex={0}
    draggable={true}
    onClick={handlePressAnchor}
    onContextMenu={() => console.log('right clicked')}
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
