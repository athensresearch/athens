# Single Instance

This chapter will cover the deployment of athens on a server that is only meant to be used for athens. Use this tutorial, if you have want to use no other reverse proxy infront of your athens deployment. If you do have another reverse proxy sitting infront of athens, [read this chapter.](./004-single-instance-no-nginx.md)

```bash
docker-compose -f ./docker-compose/003-single-instance.yml up -d
```