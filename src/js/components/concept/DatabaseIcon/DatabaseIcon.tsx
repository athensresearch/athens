import { CSSProperties } from 'react';
import styled from 'styled-components';
import { readableColor } from 'polished';

const IconWrap = styled.svg`
  font-size: var(--size, 1.5em);
  height: 1em;
  width: 1em;

  text {
    text-transform: uppercase;
    font-weight: bold;
    user-select: none;
  }
`;

export interface DatabaseIcon {
    name: string;
    color?: CSSProperties["color"];
    size?: CSSProperties["width"];
}

/**
 * Icon representing a database
 */
export const DatabaseIcon = ({
    name,
    color,
    size
}: DatabaseIcon): React.ReactElement => {
    const isEmojiIcon = /\p{Emoji}/u.test(name.trim());

    console.log(color);

    return (
        <IconWrap
            viewBox="0 0 24 24"
            style={{ "--size": size ? size : undefined }}
        >
            <rect
                fill={color ? color : "var(--link-color)"}
                height={24}
                width={24}
                rx={4}
                x={0}
                y={0}
            />
            <text
                x={12}
                y={13.5}
                fontSize={isEmojiIcon ? 16 : 20}
                textAnchor="middle"
                dominantBaseline="middle"
                fill={color ? readableColor(color) : "var(--link-color---contrast)"}
            >{isEmojiIcon ? name : name.trim().charAt(0)}</text>
        </IconWrap>)
};

DatabaseIcon.Wrap = IconWrap;