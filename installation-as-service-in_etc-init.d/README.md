Installation as a service
=========================

- copy the file 'uded' to /etc/init.d/
- make it executable with `chmod 755 uded`
- adjust the path (in `DAEMON_PATH`)

now you can use the following commands:
```
service uded start
service uded status
service uded restart
service uded stop
```

In CentOS
---------
to start automatically at each reboot by adding link to /etc/rc3.d/ with:
```
cd /etc/rc3.d/
ln -s /etc/init.d/uded S99uded
```
