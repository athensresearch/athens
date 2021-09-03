import React from 'react';
import styled from 'styled-components';
import { Fade, Popper, PopperPlacementType } from '@material-ui/core';
import { readableColor } from 'polished';

import { DOMRoot } from '../../config';

const AvatarWrap = styled.svg`
  overflow: visible;
  cursor: default;
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
        width={size}
        height={size}
        viewBox="0 0 24 24"
        ref={setAvatarEl}
        onMouseOver={() => { if (showTooltip === 'hover') setIsShowingTooltip(true) }}
        onMouseLeave={() => { if (showTooltip === 'hover') setIsShowingTooltip(false) }}
        {...props}
        style={{ ...avatarColors, ...props.style }}
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
      </Popper >
    </>
  );
};
