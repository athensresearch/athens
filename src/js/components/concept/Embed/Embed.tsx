import styled from 'styled-components';
import { classnames } from '@/utils/classnames';

const EmbedWrap = styled.div`
  border-radius: 0.25rem;
  overflow: hidden;
  display: flex;
  width: 100%;
  height: max-content;
  background: var(--background-minus-2);
  padding-block: 1px;

  &.video-16-9 {
    padding-bottom: calc((9 / 16) * 100%);
    height: 0;
    background: #000;
    position: relative;

    iframe {
      position: absolute;
      inset: 0;
      border: none;
    }
  }

  img {
    width: 100%;
    height: auto;
  }
`;

type EmbedType = "youtube" | "image";

interface EmpbedProps {
  type: EmbedType,
  url: string,
  caption?: React.ReactNode,
}

export const Embed = ({
  type,
  caption,
  url,
}: EmpbedProps) => {
  const isVideo = type === "youtube";

  return <EmbedWrap
    className={classnames(
      type && "type-" + type,
      isVideo && "video-16-9"
    )}>

    {type === "image" && <img src={url} alt={caption && caption.toString()} />}
    {type === "youtube" && <iframe
      src={url}
      width="100%"
      height="100%"
      frameBorder="0"
      allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
      allowFullScreen
    />}
  </EmbedWrap>
}
