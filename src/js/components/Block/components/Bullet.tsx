import styled from 'styled-components';

export const BulletButton = styled.button`
  flex-shrink: 0;
  grid-area: bullet;
  position: relative;
  z-index: 2;
  cursor: pointer;
  margin-right: 0.25em;
  appearance: none;
  border: 0;
  background: transparent;
  transition: all 0.05s ease;
  height: 2em;
  width: 1em;

  svg {
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
    /* box-shadow: (:4 style/DEPTH-SHADOWS); */
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
      r: 22;
      stroke-width: 12;
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

export const Bullet = ({ isClosedWithChildren, ...props }) => (
  <BulletButton
    className={['bullet', isClosedWithChildren && 'closed-with-children'].join(' ')}
    tabIndex={0}
    draggable={true}
    // onClick={() => console.log('clicked')}
    // onContextMenu={() => console.log('right clicked')}
    // onMouseOver={() => console.log('mouse over')}
    // onMouseOut={() => console.log('mouse out')}
    // onDragStart={() => console.log('drag start')}
    // onDragEnd={() => console.log('drag end')}
    {...props}
  >
    <svg viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="12" />
    </svg>
  </BulletButton>
);
