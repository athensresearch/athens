import React from 'react';
import styled from 'styled-components';
import { Fade, Popper, PopperPlacementType } from '@material-ui/core';
import { readableColor } from 'polished';

import { DOMRoot } from '../../config';

const AvatarWrap = styled.svg`
  overflow: visible;
  cursor: default;
  height: var(--size);
  width: var(--size);
`;

const Name = styled.text`
  text-anchor: middle;
  color: var(--text-color);
  transform: scale(0.8);
  transform-origin: center;
`;

const FullName = styled.span`
  background: var(--background-color);
  color: var(--text-color);
  display: inline-flex;
  width: max-content;
  line-height: 1;
  padding: 0.2em 0.5em;
  border-radius: 0.2em;
  margin: 0.25rem;
  pointer-events: none;
`;

export interface AvatarProps extends React.SVGProps<SVGSVGElement> {
  /**
* The primary color of the icon
*/
  color?: string;
  /**
   * The full username of the person
   */
  name: string;
  /**
   * Height and width of the avatar
   */
  size?: string;
  /**
   * Whether to show the user's full name. Set to 'hover' to show it when interacting with the icon.
   */
  showTooltip?: boolean | 'hover';
  /**
   * Where the tooltip should appear relative to the icon.
   */
  tooltipPlacement?: PopperPlacementType
}

/**
 * Visual representation of a human user
*/
export const Avatar = ({
  color = "#000",
  name,
  showTooltip = 'hover',
  tooltipPlacement = 'right',
  size = "1.5em",
  ...props
}: AvatarProps) => {
  const [avatarEl, setAvatarEl] = React.useState();
  const [isShowingTooltip, setIsShowingTooltip] = React.useState(showTooltip === 'hover' ? false : showTooltip);

  /**
   * CSS Variables defining the color of the Avatar elements
   */
  const avatarColors = { "--background-color": color, "--text-color": readableColor(color) }


  let initials;
  if (name) {
    initials = name.split(' ').map(word => word[0]).join('').slice(0);
  }


  return (
    <>
      <AvatarWrap
        viewBox="0 0 24 24"
        ref={setAvatarEl}
        onMouseOver={() => { if (showTooltip === 'hover') setIsShowingTooltip(true) }}
        onMouseLeave={() => { if (showTooltip === 'hover') setIsShowingTooltip(false) }}
        {...props}
        style={{ ...avatarColors, "--size": size, ...props.style }}
      >
        <circle
          cx="12"
          cy="12"
          r="12"
          fill="var(--background-color)"
          vectorEffect="non-scaling-stroke"
        />
        <Name
          x="12"
          y="18"
          fill="var(--text-color)"
          vectorEffect="non-scaling-stroke"
          fontSize="18"
        >
          {initials || name.charAt(0)}
        </Name>
      </AvatarWrap>
      <Popper
        open={showTooltip === true || isShowingTooltip}
        anchorEl={avatarEl}
        disablePortal={true}
        container={DOMRoot}
        placement={tooltipPlacement}
        transition
      >
        {({ TransitionProps }) => (
          <Fade {...TransitionProps} timeout={250}>
            <FullName
              className={"tooltip-" + tooltipPlacement}
              style={avatarColors}
            >{name}</FullName>
          </Fade>
        )}
      </Popper>
    </>
  );
};


/* 
* Wraps a horizontal series of avatars 
*/
interface AvatarStackProps {
  maskSize?: string,
  stackOverlap?: number
}
Avatar.Stack = styled.div<AvatarStackProps>`
  --mask-size: ${props => props.maskSize || '3px'};
  --stack-overlap: ${props => props.stackOverlap || '0.5'};

  ${AvatarWrap} {
    margin-inline-end: calc(var(--size) * (var(--stack-overlap) * -1));

    &:not(:last-of-type) {
      mask-image: radial-gradient(
        calc(var(--size) + var(--mask-size)) calc((var(--size) * (2 / 3)) + var(--mask-size)) at calc(100% + (100% * (1 - var(--stack-overlap)))) 50%,
        transparent 99%,
        #000 100%);
    }
  }
`;