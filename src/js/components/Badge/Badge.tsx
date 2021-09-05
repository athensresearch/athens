import React, { ReactNode } from 'react';
import styled from 'styled-components';

const BadgeEl = styled.b`
  position: absolute;
  border-radius: 100em;
  background: var(--link-color);
  font-size: var(--font-size--text-xs);
  color: #fff;
  padding: 0.125em 0.35em;
  line-height: 1;
  
  &:empty {
    padding: 0;
    width: var(--size, 0.5rem);
    height: var(--size, 0.5rem);
  }

  &.placement-top-right {
    top: 0;
    right: 0;
    transform: translate(50%, -50%);
  }
  &.placement-top-left {
    top: 0;
    left: 0;
    transform: translate(-50%, -50%);
  }
  &.placement-bottom-left {
    bottom: 0;
    left: 0;
    transform: translate(-50%, 50%);
  }
  &.placement-bottom-right {
    bottom: 0;
    right: 0;
    transform: translate(50%, 50%);
  }
`;

const BadgeWrap = styled.span`
  position: relative;
  display: inline-flex;
`;

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  badgeContent: ReactNode,
  /** Where the badge should appear relative to the content */
  placement?: 'top-right' | 'top-left' | 'bottom-left' | 'bottom-right',
}

/**
 * Wrap the content with a badge
 */
export const Badge = ({
  badgeContent,
  placement = "top-right",
  children
}: BadgeProps) => <BadgeWrap>{children}<BadgeEl className={"placement-" + placement}>{badgeContent}</BadgeEl></BadgeWrap>